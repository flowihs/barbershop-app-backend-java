package com.github.barbershop;

import com.github.barbershop.storage.service.StorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.*;

@SpringBootTest
class BarbershopAppBackendJavaApplicationTests {

    private final StorageServiceImpl storageServiceImpl = new StorageServiceImpl();

    @Test
    void contextLoads() {
    }

    @Test
    public void uploadImagesTest() {

    }
}
