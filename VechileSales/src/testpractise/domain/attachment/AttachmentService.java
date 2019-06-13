package com.dizzion.portal.domain.attachment;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead;
import static java.text.MessageFormat.format;

@Service
@Slf4j
public class AttachmentService {

    private final static String AMAZON_S3_URL = "s3.amazonaws.com";

    private final TransferManager transferManager;
    private final AmazonS3 amazonS3;

    public AttachmentService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
        this.transferManager = TransferManagerBuilder.standard().withS3Client(amazonS3).build();
    }

    public String uploadPublicAttachment(String bucketName, long attachmentId, MultipartFile file) {
        String fileKey = generateS3FileKey(attachmentId, file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        try {
            PutObjectRequest uploadRequest = new PutObjectRequest(bucketName, fileKey, file.getInputStream(), metadata);
            uploadRequest.setCannedAcl(PublicRead);
            transferManager.upload(uploadRequest).waitForUploadResult();
            return s3Url(bucketName, fileKey);
        } catch (IOException | InterruptedException e) {
            log.error("Cannot upload announcement attachment", e);
            throw new IllegalArgumentException(e);
        }
    }

    public void deletePublicAttachment(String bucketName, long attachmentId) {
        ListObjectsV2Result listObjectsV2Result = amazonS3.listObjectsV2(bucketName, attachmentId + "/");
        String[] keys = listObjectsV2Result.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .toArray(String[]::new);
        if (keys.length > 0) {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                    .withKeys(keys);
            amazonS3.deleteObjects(deleteObjectsRequest);
        }
    }

    private String generateS3FileKey(long attachmentId, String originalFileName) {
        String fileExtension = Files.getFileExtension(originalFileName);
        if (fileExtension.isEmpty()) {
            throw new IllegalArgumentException("Invalid file name. There is no file extension in file name=" + originalFileName);
        }
        String newName = UUID.randomUUID().toString() + "." + fileExtension;
        return attachmentId + "/" + newName;
    }

    private String s3Url(String bucketName, String fileKey) {
        return format("https://{0}.{1}/{2}", bucketName, AMAZON_S3_URL, fileKey);
    }
}
