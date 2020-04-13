package cn.bit.framework.storage.oss;/**
 * Created by terry on 2016/8/9.
 */

import cn.bit.framework.config.GlobalConfig;
import cn.bit.framework.storage.*;
import com.aliyun.oss.model.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;

/**
 * @author terry
 * @create 2016-08-09 10:17
 **/
public class OSSHelper {

    private static final String KEY_BUCKET = "oss.bucket.";
    private static final String KEY_DOMAIN = ".domain";

    public static String getBucket(StorageService.StorageType storageType) {
        return GlobalConfig.SYS_CONFIG.get(KEY_BUCKET + storageType.getType());
    }

    public static String getBucketDomain(StorageService.StorageType storageType) {
        return GlobalConfig.SYS_CONFIG.get(KEY_BUCKET + storageType.getType() + KEY_DOMAIN);
    }


    public static SaveResult buildSaveResult(StorageService.StorageType storageType, String key, PutObjectResult
            putObjectResult) {
        SaveResult saveResult = new SaveResult();
        saveResult.setStorageType(storageType);
        saveResult.setKey(key);
        saveResult.setMd5(putObjectResult.getETag());
        saveResult.setCallbackResponseBody(putObjectResult.getCallbackResponseBody());
        saveResult.setUrl("http://" + getBucketDomain(storageType) + "/" + key);
        return saveResult;
    }

    public static SaveResult buildSaveResult(StorageService.StorageType storageType, String key, UploadFileResult
            uploadFileResult) {
        SaveResult saveResult = new SaveResult();
        saveResult.setStorageType(storageType);
        saveResult.setKey(key);
        saveResult.setMd5(uploadFileResult.getMultipartUploadResult().getETag());
        saveResult.setCallbackResponseBody(uploadFileResult.getMultipartUploadResult().getCallbackResponseBody());
        saveResult.setUrl(uploadFileResult.getMultipartUploadResult().getLocation());
        return saveResult;
    }

    public static AppendResult buildAppendResult(StorageService.StorageType storageType, String key, AppendObjectResult
            appendObjectResult) {
        AppendResult appendResult = new AppendResult();
        appendResult.setStorageType(storageType);
        appendResult.setKey(key);
        appendResult.setNextPosition(appendObjectResult.getNextPosition());
        return appendResult;
    }


    public static PutObjectRequest buildPutRequest(StorageService.StorageType storageType, String key, File f,
                                                   StorageMetadata params) {
        PutObjectRequest request = new PutObjectRequest(getBucket(storageType), key, f);
        request.setMetadata(buildObjectMetadata(params));
        if (params.getRemoteCallback() != null) {
            request.setCallback(buildOSSCallback(params.getRemoteCallback()));
        }
        return request;
    }

    public static PutObjectRequest buildPutRequest(StorageService.StorageType storageType, String key, InputStream ins,
                                                   StorageMetadata
                                                           params) {
        PutObjectRequest request = new PutObjectRequest(getBucket(storageType), key, ins,
                buildObjectMetadata(params));
        if (params.getRemoteCallback() != null) {
            request.setCallback(buildOSSCallback(params.getRemoteCallback()));
        }
        return request;
    }

    public static UploadFileRequest buildUploadFileRequest(StorageService.StorageType storageType, String key, String
            file, int taskNum,
                                                           long partSize, StorageMetadata params) {
        UploadFileRequest request = new UploadFileRequest(getBucket(storageType), key, file, partSize, taskNum, true);
        request.setObjectMetadata(buildObjectMetadata(params));
        if (params.getRemoteCallback() != null) {
            request.setCallback(buildOSSCallback(params.getRemoteCallback()));
        }
        return request;
    }

    public static AppendObjectRequest buildAppendObjectRequest(StorageService.StorageType storageType, String key,
                                                               InputStream ins, long
                                                                       position, StorageMetadata params) {
        AppendObjectRequest request = new AppendObjectRequest(getBucket(storageType), key, ins,
                buildObjectMetadata(params)).withPosition(position);
        return request;
    }

    public static AppendObjectRequest buildAppendObjectRequest(StorageService.StorageType storageType, String key, File
            file, long
                                                                       position, StorageMetadata params) {
        AppendObjectRequest request = new AppendObjectRequest(getBucket(storageType), key, file,
                buildObjectMetadata(params)).withPosition(position);
        return request;
    }


    public static ObjectMetadata buildObjectMetadata(StorageMetadata storageMetadata) {
        ObjectMetadata metadata = new ObjectMetadata();
        if (storageMetadata.getCacheControl() != null)
            metadata.setCacheControl(storageMetadata.getCacheControl());
        if (storageMetadata.getContentType() != null)
            metadata.setContentType(storageMetadata.getContentType());
        if (storageMetadata.getContentDisposition() != null)
            metadata.setContentDisposition(storageMetadata.getContentDisposition());
        if (storageMetadata.getContentEncoding() != null)
            metadata.setContentEncoding(storageMetadata.getContentEncoding());
        if (storageMetadata.getContentLength() != null)
            metadata.setContentLength(storageMetadata.getContentLength());
        if (storageMetadata.getContentMD5() != null)
            metadata.setContentMD5(storageMetadata.getContentMD5());
        if (storageMetadata.getExpires() != null)
            metadata.setExpirationTime(storageMetadata.getExpires());
        if (storageMetadata.getAcl() != null)
            metadata.setObjectAcl(CannedAccessControlList.parse(storageMetadata.getAcl().getAcl()));
        if (storageMetadata.getUserData() != null && !storageMetadata.getUserData().isEmpty())
            metadata.setUserMetadata(storageMetadata.getUserData());
        return metadata;
    }

    public static StorageMetadata buildStorageMetadata(ObjectMetadata objectMetadata) throws ParseException {
        return new StorageMetadata.Builder()
                .cacheControl(objectMetadata.getCacheControl())
                .contentDisposition(objectMetadata.getContentDisposition())
                .contentLength(objectMetadata.getContentLength())
                .contentMD5(objectMetadata.getContentMD5())
                .contentEncoding(objectMetadata.getContentEncoding())
                .contentType(objectMetadata.getContentType())
                .expires(StringUtils.isBlank(objectMetadata.getRawExpiresValue()) ? null : objectMetadata
                        .getExpirationTime())
                .lastModified(objectMetadata.getLastModified())
                .userData(objectMetadata.getUserMetadata())
                .build();
    }

    public static Callback buildOSSCallback(StorageRemoteCallback remoteCallback) {
        Callback callback = new Callback();
        callback.setCalbackBodyType(Callback.CalbackBodyType.JSON);
        callback.setCallbackBody(remoteCallback.getBody());
        callback.setCallbackUrl(remoteCallback.getUrl());
        callback.setCallbackVar(remoteCallback.getVars());
        return callback;
    }

    public static StorageObject buildStorageObject(StorageService.StorageType storageType, String key, OSSObject
            ossObject) {
        StorageObject storageObject = new StorageObject();
        storageObject.setKey(key);
        storageObject.setStorageType(storageType);
        storageObject.setContent(ossObject.getObjectContent());
        storageObject.setMd5(ossObject.getObjectMetadata().getETag());
        try {
            storageObject.setMetadata(buildStorageMetadata(ossObject.getObjectMetadata()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return storageObject;
    }
}
