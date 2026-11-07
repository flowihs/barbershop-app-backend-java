package com.github.barbershop.barbely.controller;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.entity.UserRole;
import com.github.barbershop.account.security.AuthUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Stable API used by the Barbely Mini App.  The older Python test server exposed
 * this contract; keeping it here lets the UI move to the production Java API
 * without coupling it to the earlier provision-oriented endpoints.
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class BarbelyCompatibilityController {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final JdbcTemplate jdbc;
    private final AuthUtils auth;

    @PostConstruct
    void createSchema() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS barbely_locations (id VARCHAR(64) PRIMARY KEY, user_id BIGINT NOT NULL, city VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL, address TEXT NOT NULL, is_primary BOOLEAN NOT NULL DEFAULT FALSE)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS barbely_services (id VARCHAR(64) PRIMARY KEY, user_id BIGINT NOT NULL, name VARCHAR(255) NOT NULL, duration_minutes INTEGER NOT NULL, price INTEGER NOT NULL)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS barbely_work_days (id VARCHAR(80) PRIMARY KEY, user_id BIGINT NOT NULL, work_date DATE NOT NULL, starts_at VARCHAR(5) NOT NULL, ends_at VARCHAR(5) NOT NULL, location_id VARCHAR(64), temporary_location TEXT NOT NULL DEFAULT '', is_working BOOLEAN NOT NULL)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS barbely_weekly_work_days (id VARCHAR(80) PRIMARY KEY, user_id BIGINT NOT NULL, weekday INTEGER NOT NULL, starts_at VARCHAR(5) NOT NULL, ends_at VARCHAR(5) NOT NULL, location_id VARCHAR(64), is_working BOOLEAN NOT NULL)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS barbely_portfolio (user_id BIGINT NOT NULL, position INTEGER NOT NULL, image_url TEXT NOT NULL, PRIMARY KEY(user_id, position))");
        jdbc.execute("CREATE TABLE IF NOT EXISTS barbely_reviews (id VARCHAR(64) PRIMARY KEY, barber_id BIGINT NOT NULL, author_id BIGINT NOT NULL, rating INTEGER NOT NULL, text TEXT NOT NULL, created_at DATE NOT NULL, UNIQUE(barber_id, author_id))");
        jdbc.execute("CREATE TABLE IF NOT EXISTS barbely_bookings (id VARCHAR(64) PRIMARY KEY, client_id BIGINT NOT NULL, barber_id BIGINT NOT NULL, service_id VARCHAR(64) NOT NULL, location_id VARCHAR(64) NOT NULL, booking_date DATE NOT NULL, starts_at VARCHAR(5) NOT NULL, status VARCHAR(32) NOT NULL)");
    }

    @GetMapping("/me")
    public Map<String, Object> me() { return me(auth.getCurrentUser()); }

    @PatchMapping("/me/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, Object> body) {
        Account user = auth.getCurrentUser();
        String firstName = required(body, "firstName", "Укажите имя и фамилию");
        String lastName = required(body, "lastName", "Укажите имя и фамилию");
        user.setFirstName(firstName); user.setLastName(lastName);
        // Account is managed by the request transaction in normal JPA execution; update explicitly for clarity.
        jdbc.update("UPDATE users SET first_name = ?, last_name = ? WHERE id = ?", firstName, lastName, user.getId());
        return me(user);
    }

    @PatchMapping("/me/role")
    public Map<String, Object> setRole(@RequestBody Map<String, Object> body) {
        Account user = auth.getCurrentUser();
        String role = required(body, "role", "Неизвестная роль");
        if (!role.equals("client") && !role.equals("barber")) throw bad("Неизвестная роль");
        UserRole value = role.equals("barber") ? UserRole.BARBER : UserRole.CLIENT;
        jdbc.update("UPDATE users SET role = CAST(? AS user_role) WHERE id = ?", value.name(), user.getId());
        user.setRole(value);
        return me(user);
    }

    @PatchMapping("/barber/profile")
    public Map<String, Object> updateBarberProfile(@RequestBody Map<String, Object> body) {
        Account user = barber();
        String specialty = required(body, "specialty", "Заполните направление и описание");
        String about = required(body, "about", "Заполните направление и описание");
        jdbc.update("UPDATE users SET description = ? WHERE id = ?", specialty + "\n" + about, user.getId());
        // The compatibility schema keeps specialty in a separate service row only if needed; user description stores the public bio.
        jdbc.update("DELETE FROM barbely_services WHERE user_id = ? AND id = '__profile__'", user.getId());
        jdbc.update("INSERT INTO barbely_services(id,user_id,name,duration_minutes,price) VALUES ('__profile__',?,?,0,0)", user.getId(), specialty);
        return me(user);
    }

    @GetMapping("/barbers")
    public List<Map<String, Object>> barbers(@RequestParam(required = false) String date, @RequestParam(required = false) String city) {
        List<Long> ids = jdbc.query("SELECT DISTINCT l.user_id FROM barbely_locations l JOIN users u ON u.id=l.user_id WHERE u.role='BARBER' AND l.is_primary=TRUE" + (city == null || city.isBlank() ? "" : " AND LOWER(l.city)=LOWER(?)"), (rs, n) -> rs.getLong(1), city == null || city.isBlank() ? new Object[]{} : new Object[]{city.trim()});
        return ids.stream().map(this::summary).filter(Objects::nonNull).filter(item -> date == null || date.isBlank() || hasSlots((Long)item.get("accountId"), date)).map(this::withoutInternal).sorted(Comparator.comparing(item -> String.valueOf(item.get("name")), String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @GetMapping("/cities")
    public List<String> cities(@RequestParam(defaultValue = "") String query) {
        return jdbc.query("SELECT DISTINCT city FROM barbely_locations WHERE is_primary=TRUE AND LOWER(city) LIKE LOWER(?) ORDER BY city LIMIT 8", (rs, n) -> rs.getString(1), query.trim() + "%");
    }

    @GetMapping("/barbers/{id}")
    public Map<String, Object> barberProfile(@PathVariable String id) { return profile(account(id)); }

    @GetMapping("/barbers/{id}/slots")
    public List<String> slots(@PathVariable String id, @RequestParam String date, @RequestParam String serviceId) {
        Account barber = account(id); Service service = service(barber.getId(), serviceId);
        return slots(barber.getId(), date, service.durationMinutes());
    }

    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> body) {
        Account client = auth.getCurrentUser(); Account barber = account(required(body, "barberId", "Мастер не найден"));
        String serviceId = required(body, "serviceId", "Услуга не найдена"), date = required(body, "date", "Укажите дату"), startsAt = required(body, "startsAt", "Укажите время"), locationId = required(body, "locationId", "Укажите место");
        Service service = service(barber.getId(), serviceId); location(barber.getId(), locationId);
        if (jdbc.queryForObject("SELECT COUNT(*) FROM barbely_bookings WHERE client_id=? AND status='confirmed'", Integer.class, client.getId()) >= 2) throw bad("Можно держать не больше двух активных записей");
        if (!slots(barber.getId(), date, service.durationMinutes()).contains(startsAt)) throw bad("Это время больше недоступно");
        String id = "booking-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO barbely_bookings(id,client_id,barber_id,service_id,location_id,booking_date,starts_at,status) VALUES (?,?,?,?,?,?,?, 'confirmed')", id, client.getId(), barber.getId(), serviceId, locationId, Date.valueOf(date), startsAt);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking(id));
    }

    @GetMapping("/bookings")
    public List<Map<String, Object>> bookings() { Account user = auth.getCurrentUser(); return jdbc.query("SELECT id FROM barbely_bookings WHERE client_id=? AND status='confirmed' ORDER BY booking_date,starts_at", (rs,n) -> booking(rs.getString(1)), user.getId()); }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) { Account user = auth.getCurrentUser(); if (jdbc.update("DELETE FROM barbely_bookings WHERE id=? AND client_id=?", id, user.getId()) == 0) throw notFound("Запись не найдена"); return ResponseEntity.noContent().build(); }

    @PostMapping("/barbers/{id}/reviews")
    public ResponseEntity<Map<String, Object>> review(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Account client = auth.getCurrentUser(), barber = account(id); int rating = integer(body, "rating"); String text = required(body, "text", "Укажите оценку от 1 до 5 и текст отзыва");
        if (rating < 1 || rating > 5) throw bad("Укажите оценку от 1 до 5 и текст отзыва");
        if (jdbc.queryForObject("SELECT COUNT(*) FROM barbely_bookings WHERE client_id=? AND barber_id=?", Integer.class, client.getId(), barber.getId()) == 0) throw forbidden("Отзыв можно оставить после записи к мастеру");
        jdbc.update("DELETE FROM barbely_reviews WHERE barber_id=? AND author_id=?", barber.getId(), client.getId()); String reviewId="review-"+UUID.randomUUID().toString().substring(0,8);
        jdbc.update("INSERT INTO barbely_reviews(id,barber_id,author_id,rating,text,created_at) VALUES (?,?,?,?,?,CURRENT_DATE)", reviewId,barber.getId(),client.getId(),rating,text);
        return ResponseEntity.status(HttpStatus.CREATED).body(review(reviewId));
    }

    @GetMapping("/barber/dashboard")
    public Map<String, Object> dashboard() {
        Account user = barber(); ensureSchedule(user.getId()); Map<String,Object> out=new LinkedHashMap<>();
        out.put("barber", Map.of("id", String.valueOf(user.getTelegramId()), "name", name(user), "specialty", specialty(user.getId()), "about", about(user), "avatarUrl", safe(user.getPhotoUrl()), "coverUrl", cover(user.getId()), "socials", socials(user)));
        out.put("appointments", appointments(user.getId())); out.put("locations", locations(user.getId())); out.put("workDays", workDays(user.getId())); out.put("weeklyWorkDays", weeklyWorkDays(user.getId())); out.put("services", services(user.getId())); out.put("portfolio", portfolio(user.getId())); return out;
    }

    @PostMapping("/barber/locations")
    public ResponseEntity<Map<String,Object>> createLocation(@RequestBody Map<String,Object> body) { Account user=barber(); String id="location-"+UUID.randomUUID().toString().substring(0,8); boolean first=jdbc.queryForObject("SELECT COUNT(*) FROM barbely_locations WHERE user_id=?",Integer.class,user.getId())==0; jdbc.update("INSERT INTO barbely_locations VALUES (?,?,?,?,?,?)",id,user.getId(),required(body,"city","Укажите город, название и адрес"),required(body,"name","Укажите город, название и адрес"),required(body,"address","Укажите город, название и адрес"),first); return ResponseEntity.status(HttpStatus.CREATED).body(location(user.getId(),id)); }

    @PatchMapping("/barber/locations/{id}")
    public Map<String,Object> setPrimary(@PathVariable String id) { Account user=barber(); location(user.getId(),id); jdbc.update("UPDATE barbely_locations SET is_primary=FALSE WHERE user_id=?",user.getId()); jdbc.update("UPDATE barbely_locations SET is_primary=TRUE WHERE id=?",id); return location(user.getId(),id); }

    @PutMapping("/barber/services")
    public List<Map<String,Object>> updateServices(@RequestBody Map<String,Object> body) { Account user=barber(); Object raw=body.get("services"); if (!(raw instanceof List<?> list)) throw bad("Проверьте название, цену и длительность услуг"); jdbc.update("DELETE FROM barbely_services WHERE user_id=? AND id <> '__profile__'",user.getId()); for(Object item:list) { if (!(item instanceof Map<?,?> map)) throw bad("Проверьте название, цену и длительность услуг"); String id=string(map,"id"); String name=string(map,"name"); int duration=number(map,"durationMinutes"), price=number(map,"price"); if(id.isBlank()||name.isBlank()||duration<=0||price<=0) throw bad("Проверьте название, цену и длительность услуг"); jdbc.update("INSERT INTO barbely_services VALUES (?,?,?,?,?)",id,user.getId(),name.trim(),duration,price); } return services(user.getId()); }

    @PutMapping("/barber/portfolio") public List<String> updatePortfolio(@RequestBody Map<String,Object> body) { Account user=barber(); Object raw=body.get("portfolio"); if(!(raw instanceof List<?> list)||list.size()>12) throw bad("Добавьте до 12 изображений работ"); jdbc.update("DELETE FROM barbely_portfolio WHERE user_id=?",user.getId()); for(int i=0;i<list.size();i++){String image=String.valueOf(list.get(i)); validImage(image); jdbc.update("INSERT INTO barbely_portfolio VALUES (?,?,?)",user.getId(),i,image);} return portfolio(user.getId()); }
    @PutMapping("/barber/avatar") public String avatar(@RequestBody Map<String,Object> body) { Account user=barber(); String image=optional(body,"avatarUrl"); validImage(image); jdbc.update("UPDATE users SET photo_url=? WHERE id=?",image,user.getId()); return image; }
    @PutMapping("/barber/cover") public String cover(@RequestBody Map<String,Object> body) { Account user=barber(); String image=optional(body,"coverUrl"); validImage(image); jdbc.update("DELETE FROM barbely_portfolio WHERE user_id=? AND position=-1",user.getId()); jdbc.update("INSERT INTO barbely_portfolio VALUES (?, -1, ?)",user.getId(),image); return image; }
    @PutMapping("/barber/socials") public Map<String,String> updateSocials(@RequestBody Map<String,Object> body) { Account user=barber(); String instagram=optional(body,"instagram"),tiktok=optional(body,"tiktok"),youtube=optional(body,"youtube"); if(instagram.length()>300||tiktok.length()>300||youtube.length()>300)throw bad("Проверьте ссылки на социальные сети"); jdbc.update("UPDATE users SET instagram=?,tiktok=?,number=? WHERE id=?",instagram,tiktok,youtube,user.getId()); return socials(user); }

    @PutMapping("/barber/weekly-work-days/{id}") public Map<String,Object> weekly(@PathVariable String id,@RequestBody Map<String,Object> body){Account u=barber(); checkPlace(u.getId(),body,false); if(jdbc.update("UPDATE barbely_weekly_work_days SET weekday=?,starts_at=?,ends_at=?,location_id=?,is_working=? WHERE id=? AND user_id=?",integer(body,"weekday"),required(body,"startsAt","Проверьте день, время и место работы"),required(body,"endsAt","Проверьте день, время и место работы"),nullable(body,"locationId"),bool(body,"isWorking"),id,u.getId())==0)throw notFound("День не найден"); return weeklyDay(u.getId(),id);}
    @PutMapping("/barber/work-days/{id}") public Map<String,Object> workDay(@PathVariable String id,@RequestBody Map<String,Object> body){Account u=barber(); checkPlace(u.getId(),body,true); if(jdbc.update("UPDATE barbely_work_days SET work_date=?,starts_at=?,ends_at=?,location_id=?,temporary_location=?,is_working=? WHERE id=? AND user_id=?",Date.valueOf(required(body,"date","Проверьте дату, время и место работы")),required(body,"startsAt","Проверьте дату, время и место работы"),required(body,"endsAt","Проверьте дату, время и место работы"),nullable(body,"locationId"),optional(body,"temporaryLocation"),bool(body,"isWorking"),id,u.getId())==0)throw notFound("День не найден"); return workDay(u.getId(),id);}

    private Account barber(){ Account user=auth.getCurrentUser(); if(user.getRole()!=UserRole.BARBER && user.getRole()!=UserRole.ADMIN) throw forbidden("Выберите роль барбера"); return user; }
    private Account account(String telegramId){ try { return jdbc.queryForObject("SELECT id,telegram_id,first_name,last_name,username,description,photo_url,role FROM users WHERE telegram_id=?",(rs,n)->Account.builder().id(rs.getLong("id")).telegramId(rs.getLong("telegram_id")).firstName(rs.getString("first_name")).lastName(rs.getString("last_name")).username(rs.getString("username")).description(rs.getString("description")).photoUrl(rs.getString("photo_url")).role(UserRole.valueOf(rs.getString("role"))).build(),Long.parseLong(telegramId)); } catch(Exception e){ throw notFound("Мастер не найден"); } }
    private Map<String,Object> me(Account u){ return Map.of("firstName",safe(u.getFirstName()),"lastName",safe(u.getLastName()),"role",u.getRole()==UserRole.BARBER?"barber":"client","profileComplete",!safe(u.getFirstName()).isBlank()&&!safe(u.getLastName()).isBlank(),"barberProfileComplete",u.getRole()==UserRole.BARBER&&!specialty(u.getId()).isBlank()&&!about(u).isBlank()); }
    private Map<String,Object> summary(long id){ Account u=accountById(id); Map<String,Object> l=primary(id); if(l==null||services(id).isEmpty()||specialty(id).isBlank())return null; Map<String,Object> x=new LinkedHashMap<>();x.put("accountId",id);x.put("id",String.valueOf(u.getTelegramId()));x.put("name",name(u));x.put("specialty",specialty(id));x.put("rating",rating(id));x.put("avatarUrl",safe(u.getPhotoUrl()));x.put("coverUrl",cover(id));x.put("location",l);x.put("nextAvailableLabel","Свободное время по графику");return x; }
    private Map<String,Object> withoutInternal(Map<String,Object> source){Map<String,Object>x=new LinkedHashMap<>(source);x.remove("accountId");return x;}
    private Map<String,Object> profile(Account u){ Map<String,Object>x=summary(u.getId());if(x==null)throw notFound("Мастер не найден");x.put("about",about(u));x.put("portfolio",portfolio(u.getId()));x.put("services",services(u.getId()));x.put("reviews",reviews(u.getId()));x.put("socials",socials(u));return withoutInternal(x); }
    private Account accountById(long id){return jdbc.queryForObject("SELECT id,telegram_id,first_name,last_name,username,description,photo_url,role FROM users WHERE id=?",(rs,n)->Account.builder().id(rs.getLong("id")).telegramId(rs.getLong("telegram_id")).firstName(rs.getString("first_name")).lastName(rs.getString("last_name")).username(rs.getString("username")).description(rs.getString("description")).photoUrl(rs.getString("photo_url")).role(UserRole.valueOf(rs.getString("role"))).build(),id);}
    private String specialty(long id){return jdbc.query("SELECT name FROM barbely_services WHERE user_id=? AND id='__profile__'",(rs,n)->rs.getString(1),id).stream().findFirst().orElse("");}
    private String about(Account u){String d=safe(u.getDescription());int p=d.indexOf('\n');return p<0?d:d.substring(p+1);}
    private String cover(long id){return jdbc.query("SELECT image_url FROM barbely_portfolio WHERE user_id=? AND position=-1",(rs,n)->rs.getString(1),id).stream().findFirst().orElse("");}
    private Map<String,String> socials(Account u){return jdbc.query("SELECT instagram,tiktok,number FROM users WHERE id=?",(rs,n)->Map.of("instagram",safe(rs.getString(1)),"tiktok",safe(rs.getString(2)),"youtube",safe(rs.getString(3))),u.getId()).stream().findFirst().orElse(Map.of("instagram","","tiktok","","youtube",""));}
    private String name(Account u){return (safe(u.getFirstName())+" "+safe(u.getLastName())).trim();}
    private List<Map<String,Object>> locations(long user){return jdbc.query("SELECT id,city,name,address,is_primary FROM barbely_locations WHERE user_id=? ORDER BY is_primary DESC,name",(rs,n)->locationRow(rs),user);}
    private Map<String,Object> primary(long user){return jdbc.query("SELECT id,city,name,address,is_primary FROM barbely_locations WHERE user_id=? AND is_primary=TRUE",(rs,n)->locationRow(rs),user).stream().findFirst().orElse(null);}
    private Map<String,Object> location(long user,String id){return jdbc.query("SELECT id,city,name,address,is_primary FROM barbely_locations WHERE user_id=? AND id=?",(rs,n)->locationRow(rs),user,id).stream().findFirst().orElseThrow(()->notFound("Место не найдено"));}
    private Map<String,Object> locationRow(ResultSet r)throws SQLException{return Map.of("id",r.getString(1),"city",r.getString(2),"name",r.getString(3),"address",r.getString(4),"isPrimary",r.getBoolean(5));}
    private List<Map<String,Object>> services(long user){return jdbc.query("SELECT id,name,duration_minutes,price FROM barbely_services WHERE user_id=? AND id<>'__profile__'",(rs,n)->Map.of("id",rs.getString(1),"name",rs.getString(2),"durationMinutes",rs.getInt(3),"price",rs.getInt(4)),user);}
    private Service service(long user,String id){return jdbc.query("SELECT id,name,duration_minutes,price FROM barbely_services WHERE user_id=? AND id=?",(rs,n)->new Service(rs.getString(1),rs.getString(2),rs.getInt(3),rs.getInt(4)),user,id).stream().findFirst().orElseThrow(()->notFound("Услуга не найдена"));}
    private List<String> portfolio(long user){return jdbc.query("SELECT image_url FROM barbely_portfolio WHERE user_id=? AND position>=0 ORDER BY position",(rs,n)->rs.getString(1),user);}
    private double rating(long user){return jdbc.query("SELECT AVG(rating) FROM barbely_reviews WHERE barber_id=?",(rs,n)->rs.getDouble(1),user).stream().findFirst().orElse(0d);}
    private List<Map<String,Object>> reviews(long barber){return jdbc.query("SELECT r.id,u.first_name,u.last_name,r.rating,r.text,r.created_at FROM barbely_reviews r JOIN users u ON u.id=r.author_id WHERE r.barber_id=? ORDER BY r.created_at DESC",(rs,n)->Map.of("id",rs.getString(1),"authorName",(safe(rs.getString(2))+" "+safe(rs.getString(3))).trim(),"rating",rs.getInt(4),"text",rs.getString(5),"createdAt",rs.getDate(6).toString()),barber);}
    private Map<String,Object> review(String id){return jdbc.query("SELECT r.id,u.first_name,u.last_name,r.rating,r.text,r.created_at FROM barbely_reviews r JOIN users u ON u.id=r.author_id WHERE r.id=?",(rs,n)->Map.of("id",rs.getString(1),"authorName",(safe(rs.getString(2))+" "+safe(rs.getString(3))).trim(),"rating",rs.getInt(4),"text",rs.getString(5),"createdAt",rs.getDate(6).toString()),id).get(0);}
    private void ensureSchedule(long user){for(int i=0;i<7;i++){LocalDate d=LocalDate.now().plusDays(i);jdbc.update("INSERT INTO barbely_work_days(id,user_id,work_date,starts_at,ends_at,location_id,temporary_location,is_working) SELECT ?,?,?,?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM barbely_work_days WHERE id=?)","day-"+user+"-"+d,user,Date.valueOf(d),"10:00","20:00",null,"",false,"day-"+user+"-"+d);jdbc.update("INSERT INTO barbely_weekly_work_days(id,user_id,weekday,starts_at,ends_at,location_id,is_working) SELECT ?,?,?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM barbely_weekly_work_days WHERE id=?)","weekly-"+user+"-"+i,user,i,"10:00","20:00",null,false,"weekly-"+user+"-"+i);}}
    private List<Map<String,Object>> workDays(long u){return jdbc.query("SELECT id,work_date,starts_at,ends_at,location_id,temporary_location,is_working FROM barbely_work_days WHERE user_id=? ORDER BY work_date",(rs,n)->workDayRow(rs),u);}
    private Map<String,Object> workDay(long u,String id){return jdbc.query("SELECT id,work_date,starts_at,ends_at,location_id,temporary_location,is_working FROM barbely_work_days WHERE user_id=? AND id=?",(rs,n)->workDayRow(rs),u,id).get(0);}
    private Map<String,Object> workDayRow(ResultSet r)throws SQLException{Map<String,Object>x=new LinkedHashMap<>();x.put("id",r.getString(1));x.put("date",r.getDate(2).toString());x.put("startsAt",r.getString(3));x.put("endsAt",r.getString(4));x.put("locationId",r.getString(5));x.put("temporaryLocation",r.getString(6));x.put("isWorking",r.getBoolean(7));return x;}
    private List<Map<String,Object>> weeklyWorkDays(long u){return jdbc.query("SELECT id,weekday,starts_at,ends_at,location_id,is_working FROM barbely_weekly_work_days WHERE user_id=? ORDER BY weekday",(rs,n)->weeklyDayRow(rs),u);}
    private Map<String,Object> weeklyDay(long u,String id){return jdbc.query("SELECT id,weekday,starts_at,ends_at,location_id,is_working FROM barbely_weekly_work_days WHERE user_id=? AND id=?",(rs,n)->weeklyDayRow(rs),u,id).get(0);}
    private Map<String,Object> weeklyDayRow(ResultSet r)throws SQLException{Map<String,Object>x=new LinkedHashMap<>();x.put("id",r.getString(1));x.put("weekday",r.getInt(2));x.put("startsAt",r.getString(3));x.put("endsAt",r.getString(4));x.put("locationId",r.getString(5));x.put("isWorking",r.getBoolean(6));return x;}
    private List<Map<String,Object>> appointments(long barber){return jdbc.query("SELECT b.id,b.booking_date,b.starts_at,u.first_name,u.last_name,s.name,s.duration_minutes FROM barbely_bookings b JOIN users u ON u.id=b.client_id JOIN barbely_services s ON s.id=b.service_id WHERE b.barber_id=? AND b.status='confirmed' ORDER BY b.booking_date,b.starts_at",(rs,n)->Map.of("id",rs.getString(1),"date",rs.getDate(2).toString(),"startsAt",rs.getString(3),"clientName",(safe(rs.getString(4))+" "+safe(rs.getString(5))).trim(),"serviceName",rs.getString(6),"durationMinutes",rs.getInt(7)),barber);}
    private Map<String,Object> booking(String id){return jdbc.query("SELECT b.id,b.booking_date,b.starts_at,barber.telegram_id,barber.first_name,barber.last_name,s.name,l.id,l.city,l.name,l.address,l.is_primary FROM barbely_bookings b JOIN users barber ON barber.id=b.barber_id JOIN barbely_services s ON s.id=b.service_id JOIN barbely_locations l ON l.id=b.location_id WHERE b.id=?",(rs,n)->{Map<String,Object>x=new LinkedHashMap<>();x.put("id",rs.getString(1));x.put("date",rs.getDate(2).toString());x.put("startsAt",rs.getString(3));x.put("barberId",String.valueOf(rs.getLong(4)));x.put("barberName",(safe(rs.getString(5))+" "+safe(rs.getString(6))).trim());x.put("serviceName",rs.getString(7));x.put("location",Map.of("id",rs.getString(8),"city",rs.getString(9),"name",rs.getString(10),"address",rs.getString(11),"isPrimary",rs.getBoolean(12)));x.put("status","confirmed");return x;},id).get(0);}
    private boolean hasSlots(long barber,String date){return !services(barber).isEmpty()&&services(barber).stream().anyMatch(s->!slots(barber,date,(int)s.get("durationMinutes")).isEmpty());}
    private List<String> slots(long barber,String date,int duration){ensureSchedule(barber);Map<String,Object> day=jdbc.query("SELECT starts_at,ends_at FROM barbely_work_days WHERE user_id=? AND work_date=? AND is_working=TRUE",(rs,n)->Map.of("start",rs.getString(1),"end",rs.getString(2)),barber,Date.valueOf(date)).stream().findFirst().orElseGet(()->jdbc.query("SELECT starts_at,ends_at FROM barbely_weekly_work_days WHERE user_id=? AND weekday=? AND is_working=TRUE",(rs,n)->Map.of("start",rs.getString(1),"end",rs.getString(2)),barber,LocalDate.parse(date).getDayOfWeek().getValue()-1).stream().findFirst().orElse(null));if(day==null)return List.of();LocalTime start=LocalTime.parse((String)day.get("start")),end=LocalTime.parse((String)day.get("end"));List<String>out=new ArrayList<>();for(;!start.plusMinutes(duration).isAfter(end);start=start.plusMinutes(15)){String at=start.format(TIME);int conflicts=jdbc.queryForObject("SELECT COUNT(*) FROM barbely_bookings b JOIN barbely_services s ON s.id=b.service_id WHERE b.barber_id=? AND b.booking_date=? AND b.status='confirmed' AND ? < (b.starts_at::time + (s.duration_minutes || ' minutes')::interval) AND b.starts_at::time < (?::time + (? || ' minutes')::interval)",Integer.class,barber,Date.valueOf(date),at,at,duration);if(conflicts==0)out.add(at);}return out;}
    private void checkPlace(long user,Map<String,Object>b,boolean temporary){boolean working=bool(b,"isWorking");String location=nullable(b,"locationId"),temp=optional(b,"temporaryLocation");if(working&&(location==null&&!temporary||location!=null&&jdbc.queryForObject("SELECT COUNT(*) FROM barbely_locations WHERE user_id=? AND id=?",Integer.class,user,location)==0))throw bad("Проверьте день, время и место работы");}
    private String required(Map<String,Object>b,String key,String message){String v=optional(b,key);if(v.isBlank())throw bad(message);return v;}
    private String optional(Map<String,Object>b,String key){Object v=b.get(key);return v instanceof String s?s.trim():"";}
    private String nullable(Map<String,Object>b,String key){Object v=b.get(key);return v instanceof String s&&!s.isBlank()?s:null;}
    private int integer(Map<String,Object>b,String key){Object v=b.get(key);return v instanceof Number n?n.intValue():0;}
    private boolean bool(Map<String,Object>b,String key){return Boolean.TRUE.equals(b.get(key));}
    private String string(Map<?,?>b,String key){Object v=b.get(key);return v instanceof String s?s:"";}
    private int number(Map<?,?>b,String key){Object v=b.get(key);return v instanceof Number n?n.intValue():0;}
    private void validImage(String v){if(v.length()>3_000_000||(!v.isBlank()&&!v.startsWith("data:image/")&&!v.startsWith("http://")&&!v.startsWith("https://")))throw bad("Укажите изображение до 2 МБ");}
    private String safe(String v){return v==null?"":v;}
    private IllegalArgumentException bad(String m){return new IllegalArgumentException(m);}
    private ResponseStatusException notFound(String m){return new ResponseStatusException(HttpStatus.NOT_FOUND,m);}
    private ResponseStatusException forbidden(String m){return new ResponseStatusException(HttpStatus.FORBIDDEN,m);}
    private record Service(String id,String name,int durationMinutes,int price){}
}
