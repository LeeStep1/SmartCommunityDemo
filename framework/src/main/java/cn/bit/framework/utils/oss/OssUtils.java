package cn.bit.framework.utils.oss;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.Credentials;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.model.*;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by terry on 2016/7/6.
 */
public class OssUtils implements DisposableBean {

    private static OSSClient ossClient;


    public static OSSClient getOssClient() {
        return ossClient;
    }

    public void setOssClient(OSSClient ossClient) {
        OssUtils.ossClient = ossClient;
    }

    public static URI getEndpoint() {
        return ossClient.getEndpoint();
    }

    public static List<Bucket> listBuckets() throws OSSException, ClientException {
        return ossClient.listBuckets();
    }

    public static void deleteBucketCORSRules(GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteBucketCORSRules(genericRequest);
    }

    @Deprecated
    public static boolean isBucketExist(String bucketName) throws OSSException, ClientException {
        return ossClient.isBucketExist(bucketName);
    }

    public static List<CnameConfiguration> getBucketCname(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketCname(bucketName);
    }

    public static PutObjectResult putObject(PutObjectRequest putObjectRequest) throws OSSException, ClientException {
        return ossClient.putObject(putObjectRequest);
    }

    public static void deleteBucketCORSRules(String bucketName) throws OSSException, ClientException {
        ossClient.deleteBucketCORSRules(bucketName);
    }

    public static OSSObject getObject(GetObjectRequest getObjectRequest) throws OSSException, ClientException {
        return ossClient.getObject(getObjectRequest);
    }

    public static void deleteObject(GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteObject(genericRequest);
    }

    public static void setBucketReferer(SetBucketRefererRequest setBucketRefererRequest) throws OSSException, ClientException {
        ossClient.setBucketReferer(setBucketRefererRequest);
    }

    public static void abortMultipartUpload(AbortMultipartUploadRequest request) throws OSSException, ClientException {
        ossClient.abortMultipartUpload(request);
    }

    public static GetBucketImageResult getBucketImage(String bucketName, GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketImage(bucketName, genericRequest);
    }

    public static BucketList listBuckets(String prefix, String marker, Integer maxKeys) throws OSSException, ClientException {
        return ossClient.listBuckets(prefix, marker, maxKeys);
    }

    public static PutObjectResult putObject(URL signedUrl, String filePath, Map<String, String> requestHeaders) throws OSSException, ClientException {
        return ossClient.putObject(signedUrl, filePath, requestHeaders);
    }

    public static void setBucketTagging(String bucketName, Map<String, String> tags) throws OSSException, ClientException {
        ossClient.setBucketTagging(bucketName, tags);
    }

    public static void deleteBucketWebsite(GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteBucketWebsite(genericRequest);
    }

    public static GetImageStyleResult getImageStyle(String bucketName, String styleName) throws OSSException, ClientException {
        return ossClient.getImageStyle(bucketName, styleName);
    }

    public static List<ReplicationRule> getBucketReplication(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketReplication(bucketName);
    }

    public static boolean doesBucketExist(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.doesBucketExist(genericRequest);
    }

    public static ObjectListing listObjects(String bucketName, String prefix) throws OSSException, ClientException {
        return ossClient.listObjects(bucketName, prefix);
    }

    public static void switchCredentials(Credentials creds) {
        ossClient.switchCredentials(creds);
    }

    public static ClientConfiguration getClientConfiguration() {
        return ossClient.getClientConfiguration();
    }

    public static OSSObject getObject(String bucketName, String key) throws OSSException, ClientException {
        return ossClient.getObject(bucketName, key);
    }

    public static PutObjectResult putObject(String bucketName, String key, File file) throws OSSException, ClientException {
        return ossClient.putObject(bucketName, key, file);
    }

    public static void addBucketReplication(AddBucketReplicationRequest addBucketReplicationRequest) throws OSSException, ClientException {
        ossClient.addBucketReplication(addBucketReplicationRequest);
    }

    public static void setBucketReferer(String bucketName, BucketReferer referer) throws OSSException, ClientException {
        ossClient.setBucketReferer(bucketName, referer);
    }

    public static URL generatePresignedUrl(String bucketName, String key, Date expiration, HttpMethod method) throws ClientException {
        return ossClient.generatePresignedUrl(bucketName, key, expiration, method);
    }

    public static UploadFileResult uploadFile(UploadFileRequest uploadFileRequest) throws Throwable {
        return ossClient.uploadFile(uploadFileRequest);
    }

    public static void setBucketTagging(String bucketName, TagSet tagSet) throws OSSException, ClientException {
        ossClient.setBucketTagging(bucketName, tagSet);
    }

    public static PutObjectResult putObject(String bucketName, String key, InputStream input, ObjectMetadata metadata) throws OSSException, ClientException {
        return ossClient.putObject(bucketName, key, input, metadata);
    }

    public static void setBucketTagging(SetBucketTaggingRequest setBucketTaggingRequest) throws OSSException, ClientException {
        ossClient.setBucketTagging(setBucketTaggingRequest);
    }

    public static AccessControlList getBucketAcl(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketAcl(bucketName);
    }

    public static ObjectAcl getObjectAcl(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getObjectAcl(genericRequest);
    }

    public static ResponseMessage optionsObject(OptionsRequest request) throws OSSException, ClientException {
        return ossClient.optionsObject(request);
    }

    public static BucketWebsiteResult getBucketWebsite(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketWebsite(bucketName);
    }

    public static CopyObjectResult copyObject(CopyObjectRequest copyObjectRequest) throws OSSException, ClientException {
        return ossClient.copyObject(copyObjectRequest);
    }

    public static BucketLoggingResult getBucketLogging(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketLogging(genericRequest);
    }

    public static void deleteBucket(String bucketName) throws OSSException, ClientException {
        ossClient.deleteBucket(bucketName);
    }

    public static InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadRequest request) throws OSSException, ClientException {
        return ossClient.initiateMultipartUpload(request);
    }

    public static void setBucketCORS(SetBucketCORSRequest request) throws OSSException, ClientException {
        ossClient.setBucketCORS(request);
    }

    public static void deleteBucketLogging(String bucketName) throws OSSException, ClientException {
        ossClient.deleteBucketLogging(bucketName);
    }

    public static void deleteBucketTagging(GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteBucketTagging(genericRequest);
    }

    public static ObjectAcl getObjectAcl(String bucketName, String key) throws OSSException, ClientException {
        return ossClient.getObjectAcl(bucketName, key);
    }

    public static void deleteBucketLifecycle(String bucketName) throws OSSException, ClientException {
        ossClient.deleteBucketLifecycle(bucketName);
    }

    public static boolean doesBucketExist(String bucketName) throws OSSException, ClientException {
        return ossClient.doesBucketExist(bucketName);
    }

    public static GetImageStyleResult getImageStyle(String bucketName, String styleName, GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getImageStyle(bucketName, styleName, genericRequest);
    }

    public static CredentialsProvider getCredentialsProvider() {
        return ossClient.getCredentialsProvider();
    }

    public static void deleteImageStyle(String bucketName, String styleName, GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteImageStyle(bucketName, styleName, genericRequest);
    }

    public static void deleteBucketTagging(String bucketName) throws OSSException, ClientException {
        ossClient.deleteBucketTagging(bucketName);
    }

    public static boolean doesObjectExist(HeadObjectRequest headObjectRequest) throws OSSException, ClientException {
        return ossClient.doesObjectExist(headObjectRequest);
    }

    public static BucketLoggingResult getBucketLogging(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketLogging(bucketName);
    }

    public static TagSet getBucketTagging(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketTagging(bucketName);
    }

    public static BucketInfo getBucketInfo(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketInfo(genericRequest);
    }

    public static String getBucketLocation(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketLocation(bucketName);
    }

    public static boolean doesObjectExist(String bucketName, String key) throws OSSException, ClientException {
        return ossClient.doesObjectExist(bucketName, key);
    }

    public static PutObjectResult putObject(URL signedUrl, InputStream requestContent, long contentLength, Map<String, String> requestHeaders, boolean useChunkEncoding) throws OSSException, ClientException {
        return ossClient.putObject(signedUrl, requestContent, contentLength, requestHeaders, useChunkEncoding);
    }

    public static void setBucketStorageCapacity(SetBucketStorageCapacityRequest setBucketStorageCapacityRequest) throws OSSException, ClientException {
        ossClient.setBucketStorageCapacity(setBucketStorageCapacityRequest);
    }

    public static DownloadFileResult downloadFile(DownloadFileRequest downloadFileRequest) throws Throwable {
        return ossClient.downloadFile(downloadFileRequest);
    }

    public static List<String> getBucketReplicationLocation(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketReplicationLocation(genericRequest);
    }

    public static ObjectMetadata getObjectMetadata(String bucketName, String key) throws OSSException, ClientException {
        return ossClient.getObjectMetadata(bucketName, key);
    }

    public static BucketReplicationProgress getBucketReplicationProgress(String bucketName, String replicationRuleID) throws OSSException, ClientException {
        return ossClient.getBucketReplicationProgress(bucketName, replicationRuleID);
    }

    public static BucketInfo getBucketInfo(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketInfo(bucketName);
    }

    public static PutObjectResult putObject(URL signedUrl, String filePath, Map<String, String> requestHeaders, boolean useChunkEncoding) throws OSSException, ClientException {
        return ossClient.putObject(signedUrl, filePath, requestHeaders, useChunkEncoding);
    }

    public static PutObjectResult putObject(String bucketName, String key, InputStream input) throws OSSException, ClientException {
        return ossClient.putObject(bucketName, key, input);
    }

    public static URL generatePresignedUrl(GeneratePresignedUrlRequest request) throws ClientException {
        return ossClient.generatePresignedUrl(request);
    }

    public static void putBucketImage(PutBucketImageRequest request) throws OSSException, ClientException {
        ossClient.putBucketImage(request);
    }

    public static OSSObject getObject(URL signedUrl, Map<String, String> requestHeaders) throws OSSException, ClientException {
        return ossClient.getObject(signedUrl, requestHeaders);
    }

    public static String generatePostPolicy(Date expiration, PolicyConditions conds) {
        return ossClient.generatePostPolicy(expiration, conds);
    }

    public static ObjectListing listObjects(ListObjectsRequest listObjectsRequest) throws OSSException, ClientException {
        return ossClient.listObjects(listObjectsRequest);
    }

    public static List<String> getBucketReplicationLocation(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketReplicationLocation(bucketName);
    }

    public static PutObjectResult putObject(String bucketName, String key, File file, ObjectMetadata metadata) throws OSSException, ClientException {
        return ossClient.putObject(bucketName, key, file, metadata);
    }

    public static BucketWebsiteResult getBucketWebsite(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketWebsite(genericRequest);
    }

    public static UploadPartCopyResult uploadPartCopy(UploadPartCopyRequest request) throws OSSException, ClientException {
        return ossClient.uploadPartCopy(request);
    }

    public static void shutdown() {
        ossClient.shutdown();
    }

    public static CopyObjectResult copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) throws OSSException, ClientException {
        return ossClient.copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
    }

    public static ObjectMetadata getObjectMetadata(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getObjectMetadata(genericRequest);
    }

    public static List<ReplicationRule> getBucketReplication(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketReplication(genericRequest);
    }

    public static Bucket createBucket(String bucketName) throws OSSException, ClientException {
        return ossClient.createBucket(bucketName);
    }

    public static void setObjectAcl(SetObjectAclRequest setObjectAclRequest) throws OSSException, ClientException {
        ossClient.setObjectAcl(setObjectAclRequest);
    }

    public static String calculatePostSignature(String postPolicy) throws ClientException {
        return ossClient.calculatePostSignature(postPolicy);
    }

    public static List<SetBucketCORSRequest.CORSRule> getBucketCORSRules(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketCORSRules(bucketName);
    }

    public static void deleteBucketImage(String bucketName, GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteBucketImage(bucketName, genericRequest);
    }

    public static void putImageStyle(PutImageStyleRequest putImageStyleRequest) throws OSSException, ClientException {
        ossClient.putImageStyle(putImageStyleRequest);
    }

    public static void deleteBucket(GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteBucket(genericRequest);
    }

    public static void deleteBucketReplication(String bucketName, String replicationRuleID) throws OSSException, ClientException {
        ossClient.deleteBucketReplication(bucketName, replicationRuleID);
    }

    public static GetBucketImageResult getBucketImage(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketImage(bucketName);
    }

    public static void deleteBucketCname(DeleteBucketCnameRequest deleteBucketCnameRequest) throws OSSException, ClientException {
        ossClient.deleteBucketCname(deleteBucketCnameRequest);
    }

    public static List<Style> listImageStyle(String bucketName, GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.listImageStyle(bucketName, genericRequest);
    }

    public static PutObjectResult putObject(URL signedUrl, InputStream requestContent, long contentLength, Map<String, String> requestHeaders) throws OSSException, ClientException {
        return ossClient.putObject(signedUrl, requestContent, contentLength, requestHeaders);
    }

    public static void deleteBucketWebsite(String bucketName) throws OSSException, ClientException {
        ossClient.deleteBucketWebsite(bucketName);
    }

    public static SimplifiedObjectMeta getSimplifiedObjectMeta(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getSimplifiedObjectMeta(genericRequest);
    }

    public static void deleteObject(String bucketName, String key) throws OSSException, ClientException {
        ossClient.deleteObject(bucketName, key);
    }

    public static void setBucketLifecycle(SetBucketLifecycleRequest setBucketLifecycleRequest) throws OSSException, ClientException {
        ossClient.setBucketLifecycle(setBucketLifecycleRequest);
    }

    public static ObjectMetadata getObject(GetObjectRequest getObjectRequest, File file) throws OSSException, ClientException {
        return ossClient.getObject(getObjectRequest, file);
    }

    public static void setBucketLogging(SetBucketLoggingRequest request) throws OSSException, ClientException {
        ossClient.setBucketLogging(request);
    }

    public static UserQos getBucketStorageCapacity(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketStorageCapacity(genericRequest);
    }

    public static TagSet getBucketTagging(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketTagging(genericRequest);
    }

    public static void setBucketAcl(String bucketName, CannedAccessControlList cannedACL) throws OSSException, ClientException {
        ossClient.setBucketAcl(bucketName, cannedACL);
    }

    public static ObjectListing listObjects(String bucketName) throws OSSException, ClientException {
        return ossClient.listObjects(bucketName);
    }

    public static void deleteBucketReplication(DeleteBucketReplicationRequest deleteBucketReplicationRequest) throws OSSException, ClientException {
        ossClient.deleteBucketReplication(deleteBucketReplicationRequest);
    }

    public static void setEndpoint(String endpoint) {
        ossClient.setEndpoint(endpoint);
    }

    public static CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request) throws OSSException, ClientException {
        return ossClient.completeMultipartUpload(request);
    }

    public static AppendObjectResult appendObject(AppendObjectRequest appendObjectRequest) throws OSSException, ClientException {
        return ossClient.appendObject(appendObjectRequest);
    }

    public static UserQos getBucketStorageCapacity(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketStorageCapacity(bucketName);
    }

    public static AccessControlList getBucketAcl(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketAcl(genericRequest);
    }

    public static List<CnameConfiguration> getBucketCname(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketCname(genericRequest);
    }

    public static BucketReferer getBucketReferer(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketReferer(genericRequest);
    }

    public static MultipartUploadListing listMultipartUploads(ListMultipartUploadsRequest request) throws OSSException, ClientException {
        return ossClient.listMultipartUploads(request);
    }

    public static List<Style> listImageStyle(String bucketName) throws OSSException, ClientException {
        return ossClient.listImageStyle(bucketName);
    }

    public static String getBucketLocation(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketLocation(genericRequest);
    }

    public static BucketReferer getBucketReferer(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketReferer(bucketName);
    }

    public static List<LifecycleRule> getBucketLifecycle(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketLifecycle(genericRequest);
    }

    public static URL generatePresignedUrl(String bucketName, String key, Date expiration) throws ClientException {
        return ossClient.generatePresignedUrl(bucketName, key, expiration);
    }

    public static void addBucketCname(AddBucketCnameRequest addBucketCnameRequest) throws OSSException, ClientException {
        ossClient.addBucketCname(addBucketCnameRequest);
    }

    public static void deleteBucketLifecycle(GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteBucketLifecycle(genericRequest);
    }

    public static UploadPartResult uploadPart(UploadPartRequest request) throws OSSException, ClientException {
        return ossClient.uploadPart(request);
    }

    public static Bucket createBucket(CreateBucketRequest createBucketRequest) throws OSSException, ClientException {
        return ossClient.createBucket(createBucketRequest);
    }

    public static void deleteBucketCname(String bucketName, String domain) throws OSSException, ClientException {
        ossClient.deleteBucketCname(bucketName, domain);
    }

    public static void deleteBucketLogging(GenericRequest genericRequest) throws OSSException, ClientException {
        ossClient.deleteBucketLogging(genericRequest);
    }

    public static void setBucketWebsite(SetBucketWebsiteRequest setBucketWebSiteRequest) throws OSSException, ClientException {
        ossClient.setBucketWebsite(setBucketWebSiteRequest);
    }

    public static void setBucketStorageCapacity(String bucketName, UserQos userQos) throws OSSException, ClientException {
        ossClient.setBucketStorageCapacity(bucketName, userQos);
    }

    public static DeleteObjectsResult deleteObjects(DeleteObjectsRequest deleteObjectsRequest) throws OSSException, ClientException {
        return ossClient.deleteObjects(deleteObjectsRequest);
    }

    public static BucketList listBuckets(ListBucketsRequest listBucketsRequest) throws OSSException, ClientException {
        return ossClient.listBuckets(listBucketsRequest);
    }

    public static void setBucketAcl(SetBucketAclRequest setBucketAclRequest) throws OSSException, ClientException {
        ossClient.setBucketAcl(setBucketAclRequest);
    }

    public static PartListing listParts(ListPartsRequest request) throws OSSException, ClientException {
        return ossClient.listParts(request);
    }

    public static void deleteImageStyle(String bucketName, String styleName) throws OSSException, ClientException {
        ossClient.deleteImageStyle(bucketName, styleName);
    }

    public static List<LifecycleRule> getBucketLifecycle(String bucketName) throws OSSException, ClientException {
        return ossClient.getBucketLifecycle(bucketName);
    }

    public static SimplifiedObjectMeta getSimplifiedObjectMeta(String bucketName, String key) throws OSSException, ClientException {
        return ossClient.getSimplifiedObjectMeta(bucketName, key);
    }

    public static BucketReplicationProgress getBucketReplicationProgress(GetBucketReplicationProgressRequest getBucketReplicationProgressRequest) throws OSSException, ClientException {
        return ossClient.getBucketReplicationProgress(getBucketReplicationProgressRequest);
    }

    public static void setObjectAcl(String bucketName, String key, CannedAccessControlList cannedACL) throws OSSException, ClientException {
        ossClient.setObjectAcl(bucketName, key, cannedACL);
    }

    public static List<SetBucketCORSRequest.CORSRule> getBucketCORSRules(GenericRequest genericRequest) throws OSSException, ClientException {
        return ossClient.getBucketCORSRules(genericRequest);
    }

    public static void deleteBucketImage(String bucketName) throws OSSException, ClientException {
        ossClient.deleteBucketImage(bucketName);
    }

    @Override
    public void destroy() throws Exception {
        ossClient.shutdown();
    }
}
