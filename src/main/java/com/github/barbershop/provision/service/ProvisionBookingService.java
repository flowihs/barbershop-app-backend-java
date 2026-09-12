package com.github.barbershop.provision.service;

import com.github.barbershop.account.entity.Account;
import com.github.barbershop.account.service.AccountService;
import com.github.barbershop.provision.dto.BookingSlotRequest;
import com.github.barbershop.provision.entity.ProvisionBooking;
import com.github.barbershop.provision.entity.ProvisionSlot;
import com.github.barbershop.provision.exception.ProvisionSlotNotAvailableException;
import com.github.barbershop.provision.exception.ProvisionSlotNotFoundException;
import com.github.barbershop.provision.repository.ProvisionBookingRepository;
import com.github.barbershop.provision.repository.ProvisionSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProvisionBookingService {
    private final ProvisionBookingRepository provisionBookingRepository;
    private final ProvisionSlotRepository provisionSlotRepository;
    private final AccountService accountService;

    @Transactional
    public void bookingSlot(final BookingSlotRequest dto, final Long userId) {
        ProvisionSlot slot = provisionSlotRepository.findById(dto.getSlotId())
                .orElseThrow(ProvisionSlotNotFoundException::new);

        Account account = accountService.findById(userId);

        Optional<ProvisionBooking> existingBookingOpt =
                provisionBookingRepository.findByProvisionSlot(slot);

        if (existingBookingOpt.isPresent()) {
            handleExistingBooking(existingBookingOpt.get(), slot, userId);
        } else {
            createBooking(account, slot);
        }
    }

    private void createBooking(final Account account, final ProvisionSlot slot) {
        ProvisionBooking newBooking = ProvisionBooking.builder()
                .account(account)
                .provisionSlot(slot)
                .build();

        slot.setAvailable(false);
        provisionSlotRepository.save(slot);
        provisionBookingRepository.save(newBooking);
    }

    private void handleExistingBooking(final ProvisionBooking existingBooking,
                                       final ProvisionSlot slot,
                                       final Long userId) {
        if (!existingBooking.getAccount().getId().equals(userId)) {
            throw new ProvisionSlotNotAvailableException();
        }

        cancelBooking(existingBooking, slot);
    }

    private void cancelBooking(final ProvisionBooking existingBooking, final ProvisionSlot slot) {
        slot.setAvailable(true);
        provisionSlotRepository.save(slot);
        provisionBookingRepository.delete(existingBooking);
    }
}