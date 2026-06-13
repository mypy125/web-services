package com.mygitgor.user_service.domain.port.outgoing;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileStoragePort {
    Mono<String> uploadFile(FilePart file, String directory);
    Mono<Void> deleteFile(String fileUrl);
}
