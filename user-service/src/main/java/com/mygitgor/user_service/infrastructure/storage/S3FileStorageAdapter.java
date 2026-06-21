package com.mygitgor.user_service.infrastructure.storage;

import com.mygitgor.user_service.domain.port.outgoing.FileStoragePort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class S3FileStorageAdapter implements FileStoragePort {

    @Override
    public Mono<String> uploadFile(FilePart file, String directory) {
        return null;
    }

    @Override
    public Mono<Void> deleteFile(String fileUrl) {
        return null;
    }
}
