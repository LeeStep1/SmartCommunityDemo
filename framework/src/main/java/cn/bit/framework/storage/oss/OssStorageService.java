package cn.bit.framework.storage.oss;/**
 * Created by terry on 2016/8/8.
 */

import cn.bit.framework.storage.*;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import cn.bit.framework.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static cn.bit.framework.storage.oss.OSSHelper.*;


/**
 * 存储服务接口，OSS实现类
 *
 * @author terry
 * @create 2016-08-08 11:53
 **/
public class OssStorageService implements StorageService, DisposableBean {

    //private static final

    private Executor executor;

    private OSSClient ossClient;

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public OSSClient getOssClient() {
        return ossClient;
    }

    public void setOssClient(OSSClient ossClient) {
        this.ossClient = ossClient;
    }

    private static final String STORAGE_NAME = "oss";

    @Override
    public String getName() {
        return STORAGE_NAME;
    }



    @Override
    public SaveResult save(StorageType storageType, String key, File file) throws StorageException {
        try {

            PutObjectResult rs = ossClient.putObject(getBucket(storageType), key, file);
            return buildSaveResult(storageType, key, rs);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        }
    }



    @Override
    public SaveResult save(StorageType storageType, String key, File file, StorageMetadata params) throws
            StorageException {
        try {

            PutObjectResult rs = ossClient.putObject(buildPutRequest(storageType, key, file, params));
            return buildSaveResult(storageType, key, rs);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        }
    }

    @Override
    public CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, File file) throws
            StorageException {

        if (executor == null)//如线程池为空，采用ForkJoinPool.commonPool执行
            return CompletableFuture.supplyAsync(() -> this.save(storageType, key, file));
        return CompletableFuture.supplyAsync(() -> this.save(storageType, key, file), executor);
    }

    @Override
    public CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, File file, Executor executor)
            throws StorageException {
        if (executor == null)
            return this.saveAsync(storageType, key, file);
        return CompletableFuture.supplyAsync(() -> this.save(storageType, key, file), executor);
    }

    @Override
    public CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key,
                                                   File file, StorageMetadata params) throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> save(storageType, key, file, params));
        return CompletableFuture.supplyAsync(() -> save(storageType, key, file, params), executor);
    }

    @Override
    public CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, File file, StorageMetadata
            params, Executor executor) throws StorageException {
        if (executor == null)
            return this.saveAsync(storageType, key, file, params);
        return CompletableFuture.supplyAsync(() -> save(storageType, key, file, params), executor);
    }

    @Override
    public SaveResult save(StorageType storageType, String key, InputStream ins) throws StorageException {
        try {
            PutObjectResult rs = ossClient.putObject(getBucket(storageType), key, ins);
            return buildSaveResult(storageType, key, rs);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        }

    }

    @Override
    public CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, InputStream ins)
            throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> save(storageType, key, ins));
        return CompletableFuture.supplyAsync(() -> save(storageType, key, ins), executor);
    }

    @Override
    public CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, InputStream ins, Executor
            executor) throws StorageException {
        if (executor == null)
            return this.saveAsync(storageType, key, ins);
        return CompletableFuture.supplyAsync(() -> save(storageType, key, ins), executor);
    }

    @Override
    public SaveResult save(StorageType storageType, String key, InputStream ins, StorageMetadata params) throws
            StorageException {
        try {
            PutObjectResult rs = ossClient.putObject(buildPutRequest(storageType, key, ins, params));
            return buildSaveResult(storageType, key, rs);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        }
    }

    @Override
    public CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, InputStream ins,
                                                   StorageMetadata params) throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> save(storageType, key, ins, params));
        return CompletableFuture.supplyAsync(() -> save(storageType, key, ins, params), executor);
    }

    @Override
    public CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, InputStream ins,
                                                   StorageMetadata params, Executor executor) throws StorageException {
        if (executor == null)
            return this.saveAsync(storageType, key, ins, params);
        return CompletableFuture.supplyAsync(() -> save(storageType, key, ins, params), executor);
    }

    @Override
    public SaveResult resumeSave(StorageType storageType, String key, String filePath, int taskNum, long partSize)
            throws StorageException {
        UploadFileRequest request = new UploadFileRequest(getBucket(storageType), key, filePath, partSize, taskNum,
                true);
        try {
            UploadFileResult rs = ossClient.uploadFile(request);
            return buildSaveResult(storageType, key, rs);
        } catch (Throwable throwable) {
            throw new StorageException(throwable.getMessage());
        }
    }

    @Override
    public CompletableFuture<SaveResult> resumeSaveAsync(StorageType storageType, String key, String filePath, int
            taskNum, long partSize) throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> resumeSave(storageType, key, filePath, taskNum, partSize));
        return CompletableFuture.supplyAsync(() -> resumeSave(storageType, key, filePath, taskNum, partSize), executor);
    }

    @Override
    public CompletableFuture<SaveResult> resumeSaveAsync(StorageType storageType, String key, String filePath, int
            taskNum, long partSize, Executor executor) throws StorageException {
        if (executor == null)
            return this.resumeSaveAsync(storageType, key, filePath, taskNum, partSize);
        return CompletableFuture.supplyAsync(() -> resumeSave(storageType, key, filePath, taskNum, partSize), executor);
    }

    @Override
    public SaveResult resumeSave(StorageType storageType, String key, String filePath, int taskNum, long partSize,
                                 StorageMetadata params) throws StorageException {
        UploadFileRequest request = buildUploadFileRequest(storageType, key, filePath, taskNum, partSize, params);
        try {
            UploadFileResult rs = ossClient.uploadFile(request);
            return buildSaveResult(storageType, key, rs);
        } catch (Throwable throwable) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, throwable.getMessage());
        }
    }


    @Override
    public CompletableFuture<SaveResult> resumeSaveAsync(StorageType storageType, String key, String filePath, int
            taskNum, long partSize, StorageMetadata params) throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> resumeSave(storageType, key, filePath, taskNum, partSize,
                    params));
        return CompletableFuture.supplyAsync(() -> resumeSave(storageType, key, filePath, taskNum, partSize, params),
                executor);
    }


    @Override
    public CompletableFuture<SaveResult> resumeSaveAsync(StorageType storageType, String key, String filePath, int
            taskNum, long partSize, StorageMetadata params, Executor executor) throws StorageException {
        if (executor == null)
            return this.resumeSaveAsync(storageType, key, filePath, taskNum, partSize, params);
        return CompletableFuture.supplyAsync(() -> resumeSave(storageType, key, filePath, taskNum, partSize, params),
                executor);
    }

    @Override
    public AppendResult appendSave(StorageType storageType, String key, InputStream ins, Long position) throws
            StorageException {

        AppendObjectRequest request = new AppendObjectRequest(getBucket(storageType), key, ins).withPosition(position);
        try {
            AppendObjectResult rs = ossClient.appendObject(request);
            return buildAppendResult(storageType, key, rs);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        }

    }

    @Override
    public CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, InputStream ins, Long
            position) throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, ins, position));
        return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, ins, position), executor);
    }

    @Override
    public CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, InputStream ins, Long
            position, Executor executor) throws StorageException {
        if (executor == null)
            return this.appendSaveAsync(storageType, key, ins, position);
        return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, ins, position), executor);
    }

    @Override
    public AppendResult appendSave(StorageType storageType, String key, File file, Long position) throws
            StorageException {
        AppendObjectRequest request = new AppendObjectRequest(getBucket(storageType), key, file).withPosition(position);
        try {
            AppendObjectResult rs = ossClient.appendObject(request);
            return buildAppendResult(storageType, key, rs);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, File file, Long
            position) throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, file, position));
        return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, file, position), executor);
    }

    @Override
    public CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, File file, Long
            position, Executor executor) throws StorageException {
        if (executor == null)
            return this.appendSaveAsync(storageType, key, file, position);
        return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, file, position), executor);
    }

    @Override
    public AppendResult appendSave(StorageType storageType, String key, File file, Long position, StorageMetadata
            params)
            throws StorageException {
        AppendObjectRequest request = buildAppendObjectRequest(storageType, key, file, position, params);
        try {
            AppendObjectResult rs = ossClient.appendObject(request);
            return buildAppendResult(storageType, key, rs);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, File file, Long
            position, StorageMetadata params) throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, file, position, params));
        return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, file, position, params), executor);
    }

    @Override
    public CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, File file, Long
            position, StorageMetadata params, Executor executor) throws StorageException {
        if (executor == null)
            return this.appendSaveAsync(storageType, key, file, position, params);
        return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, file, position, params), executor);
    }

    @Override
    public AppendResult appendSave(StorageType storageType, String key, InputStream ins, Long position, StorageMetadata
            params) throws StorageException {
        AppendObjectRequest request = buildAppendObjectRequest(storageType, key, ins, position, params);
        try {
            AppendObjectResult rs = ossClient.appendObject(request);
            return buildAppendResult(storageType, key, rs);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_SAVE_FAILD, e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, InputStream ins, Long
            position, StorageMetadata params) throws StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, ins, position, params));
        return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, ins, position, params), executor);
    }

    @Override
    public CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, InputStream ins, Long
            position, StorageMetadata params, Executor executor) throws StorageException {
        if (executor == null)
            return this.appendSaveAsync(storageType, key, ins, position, params);
        return CompletableFuture.supplyAsync(() -> appendSave(storageType, key, ins, position, params), executor);
    }

    @Override
    public void delete(StorageType storageType, String key) throws StorageException {
        try {
            ossClient.deleteObject(getBucket(storageType), key);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_DELETE_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_DELETE_FAILD, e.getMessage());
        }
    }

    @Override
    public CompletableFuture<StorageResult> deleteAsync(StorageType storageType, String key) throws
            StorageException {
        if (executor == null)
            return CompletableFuture.supplyAsync(() -> {
                delete(storageType, key);
                return new StorageResult(storageType, key);
            });
        return CompletableFuture.supplyAsync(() -> {
            delete(storageType, key);
            return new StorageResult(storageType, key);
        }, executor);
    }

    @Override
    public CompletableFuture<StorageResult> deleteAsync(StorageType storageType, String key, Executor executor) throws
            StorageException {
        if (executor == null)
            return deleteAsync(storageType, key);
        return CompletableFuture.supplyAsync(() -> {
            delete(storageType, key);
            return new StorageResult(storageType, key);
        }, executor);
    }

    @Override
    public StorageObject get(StorageType storageType, String key) throws StorageException {
        try {
            OSSObject ossObject = ossClient.getObject(getBucket(storageType), key);
            return buildStorageObject(storageType, key, ossObject);
        } catch (OSSException e) {
            throw new StorageException(StorageException.STORAGE_DOWNLOAD_FAILD, e.getMessage());
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_DOWNLOAD_FAILD, e.getMessage());
        }

    }

    @Override
    public DownloadResult resumeGet(StorageType storageType, String key, String file, int taskNum, long partSize) {
        DownloadFileRequest downloadFileRequest = new DownloadFileRequest(getBucket(storageType), key, file, partSize,
                taskNum, true);
        try {
            DownloadFileResult rs = ossClient.downloadFile(downloadFileRequest);
            DownloadResult downloadResult = new DownloadResult();
            downloadResult.setStorageType(storageType);
            downloadResult.setKey(key);
            downloadResult.setMetadata(buildStorageMetadata(rs.getObjectMetadata()));
            return downloadResult;
        } catch (Throwable throwable) {
            throw new StorageException(StorageException.STORAGE_DOWNLOAD_FAILD, throwable.getMessage());
        }
    }

    @Override
    public String getDomain(StorageType storageType) throws StorageException {
        List<CnameConfiguration> cnameConfigurations = ossClient.getBucketCname(getBucket(storageType));
        if (cnameConfigurations == null || cnameConfigurations.size() == 0)
            return OSSHelper.getBucketDomain(storageType);
        return cnameConfigurations.get(0).getDomain();
    }

    @Override
    public String generateUrl(StorageType storageType, String key) throws StorageException {
        return generateUrl(storageType, key, DateUtils.addHour(new Date(), 24));
    }

    @Override
    public String generateUrl(StorageType storageType, String key, Date expiration) throws StorageException {
        try {
            URL url = ossClient.generatePresignedUrl(getBucket(storageType), key, expiration);
            return new URL(url.getProtocol(), getDomain(storageType), url.getFile()).toString();
        } catch (ClientException e) {
            throw new StorageException(StorageException.STORAGE_OPER_FAILD, e.getMessage());
        } catch (MalformedURLException e) {
            throw new StorageException(StorageException.STORAGE_OPER_FAILD, e.getMessage());
        }
    }

    @Override
    public Link getLink(StorageType storageType, String key) {
        return getLink(storageType, key, DateUtils.addHour(new Date(), 3));
    }

    @Override
    public Link getLink(StorageType storageType, String key, Date expiration) {
        ObjectMetadata metadata = null;
        try {
            metadata = ossClient.getObjectMetadata(getBucket(storageType), key);
            if (metadata == null)
                return null;
            Link link = new Link();
            link.setProvider(this.getName());
            link.setMd5(metadata.getETag());
            link.setSize(metadata.getContentLength());
            link.setKey(key);
            try {
                if (StringUtils.isNotBlank(metadata.getRawExpiresValue()))
                    link.setExpires(metadata.getExpirationTime().getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //ObjectAcl objectAcl = ossClient.getObjectAcl(getBucket(storageType), key);
            if (isPrivateObject(getBucket(storageType), key)) {
                link.setUrl(generateUrl(storageType, key, expiration));
            } else {
                link.setUrl("http://" + getBucketDomain(storageType) + "/" + key);
            }
            return link;
        } catch (OSSException e) {
            return null;
        } catch (ClientException e) {
            return null;
        }
    }

    private boolean isPrivateObject(String bucket, String key) {
        ObjectAcl objectAcl = ossClient.getObjectAcl(bucket, key);
        if (objectAcl.getPermission() == ObjectPermission.Private)
            return true;
        if (objectAcl.getPermission() == ObjectPermission.Default) {
            AccessControlList accessControlList = ossClient.getBucketAcl(bucket);
            return accessControlList.getGrants().isEmpty();
        }
        return false;
    }

    @Override
    public void destroy() throws Exception {
        ossClient.shutdown();
    }

}
