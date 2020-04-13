package cn.bit.framework.storage;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 存储服务接口
 * Created by terry on 2016/8/6.
 */
public interface StorageService {

    /**
     * @return
     */
    String getName();

    /**
     * 保存文件
     *
     * @param storageType 存储类别 如:验证码、头像等
     * @param key         文件标识
     * @param file        文件
     * @return 保存结果
     * @throws StorageException
     */
    SaveResult save(StorageType storageType, String key, File file) throws StorageException;

    /**
     * 保存文件
     *
     * @param storageType 存储类别 如:验证码、头像等
     * @param key         文件标识
     * @param file        文件
     * @param params
     * @return 保存结果
     * @throws StorageException
     */
    SaveResult save(StorageType storageType, String key, File file, StorageMetadata params) throws StorageException;

    /**
     * 异步回调方式，保存文件
     *
     * @param storageType 存储类别 如:验证码、头像等
     * @param key         文件标识
     * @param file        文件
     * @return 保存结果
     * @throws StorageException
     */
    CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, File file) throws StorageException;

    /**
     * 异步回调方式，保存文件
     *
     * @param storageType 存储类别 如:验证码、头像等
     * @param key         文件标识
     * @param file        文件
     * @return 保存结果
     * @throws StorageException
     */
    CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, File file, Executor executor) throws
            StorageException;

    /**
     * 异步回调方式，保存文件
     *
     * @param storageType 存储类别 如:验证码、头像等
     * @param key         文件标识
     * @param file        文件
     * @param params      附加参数
     * @throws StorageException
     */
    CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key,
                                            File file, StorageMetadata params) throws StorageException;

    /**
     * 异步回调方式，保存文件
     *
     * @param storageType 存储类别 如:验证码、头像等
     * @param key         文件标识
     * @param file        文件
     * @param params      附加参数
     * @throws StorageException
     */
    CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key,
                                            File file, StorageMetadata params, Executor executor) throws
            StorageException;

    /**
     * 保存数据流
     *
     * @param storageType 存储类别 如:验证码、头像等
     * @param key         文件标识
     * @param ins         数据流
     * @return 保存结果
     * @throws StorageException
     */
    SaveResult save(StorageType storageType, String key, InputStream ins) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param ins
     * @return
     * @throws StorageException
     */
    CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, InputStream ins) throws
            StorageException;

    /**
     * @param storageType
     * @param key
     * @param ins
     * @return
     * @throws StorageException
     */
    CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, InputStream ins, Executor executor)
            throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param ins
     * @param params
     * @return
     * @throws StorageException
     */
    SaveResult save(StorageType storageType, String key, InputStream ins, StorageMetadata params) throws
            StorageException;


    /**
     * 异步回调方式，保存流
     *
     * @param storageType
     * @param key
     * @param ins
     * @param params
     * @return
     * @throws StorageException
     */
    CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, InputStream ins, StorageMetadata
            params) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param ins
     * @param params
     * @param executor
     * @return
     * @throws StorageException
     */
    CompletableFuture<SaveResult> saveAsync(StorageType storageType, String key, InputStream ins, StorageMetadata
            params, Executor executor) throws StorageException;

    /**
     * 断点续传
     *
     * @param storageType
     * @param key
     * @param filePath
     * @param taskNum
     * @param partSize
     * @return
     * @throws StorageException
     */
    SaveResult resumeSave(StorageType storageType, String key, String filePath, int taskNum, long partSize) throws
            StorageException;

    /**
     * 断点续传
     *
     * @param storageType
     * @param key
     * @param filePath
     * @param taskNum
     * @param partSize
     * @return
     * @throws StorageException
     */
    SaveResult resumeSave(StorageType storageType, String key, String filePath, int taskNum, long partSize,
                          StorageMetadata params) throws StorageException;

    /**
     * 异步回调方式，断点续传
     *
     * @param storageType
     * @param key
     * @param filePath
     * @param taskNum
     * @param partSize
     * @return
     * @throws StorageException
     */
    CompletableFuture<SaveResult> resumeSaveAsync(StorageType storageType, String key, String filePath, int taskNum,
                                                  long partSize) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param filePath
     * @param taskNum
     * @param partSize
     * @param executor
     * @return
     * @throws StorageException
     */
    CompletableFuture<SaveResult> resumeSaveAsync(StorageType storageType, String key, String filePath, int taskNum,
                                                  long partSize, Executor executor) throws StorageException;

    /**
     * 断点续传
     *
     * @param storageType
     * @param key
     * @param filePath
     * @param taskNum
     * @param partSize
     * @return
     * @throws StorageException
     */
    CompletableFuture<SaveResult> resumeSaveAsync(StorageType storageType, String key, String filePath, int taskNum,
                                                  long partSize, StorageMetadata params) throws StorageException;


    /**
     * @param storageType
     * @param key
     * @param filePath
     * @param taskNum
     * @param partSize
     * @param params
     * @param executor
     * @return
     * @throws StorageException
     */
    CompletableFuture<SaveResult> resumeSaveAsync(StorageType storageType, String key, String filePath, int taskNum,
                                                  long partSize, StorageMetadata params, Executor executor) throws
            StorageException;

    /**
     * 追加上传
     *
     * @param storageType
     * @param key
     * @param file
     * @param position
     * @return
     * @throws StorageException
     */
    AppendResult appendSave(StorageType storageType, String key, File file, Long position) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param file
     * @param position
     * @throws StorageException
     */
    CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, File file, Long position)
            throws
            StorageException;

    /**
     * @param storageType
     * @param key
     * @param file
     * @param position
     * @throws StorageException
     */
    CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, File file, Long position,
                                                    Executor executor) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param file
     * @param position
     * @param params
     * @return
     * @throws StorageException
     */
    AppendResult appendSave(StorageType storageType, String key, File file, Long position, StorageMetadata params)
            throws
            StorageException;

    /**
     * @param storageType
     * @param key
     * @param file
     * @param position
     * @param params
     * @throws StorageException
     */
    CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, File file, Long position,
                                                    StorageMetadata params) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param file
     * @param position
     * @param params
     * @param executor
     * @return
     * @throws StorageException
     */
    CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, File file, Long position,
                                                    StorageMetadata params, Executor executor) throws StorageException;

    /**
     * 追加上传
     *
     * @param storageType
     * @param key
     * @param ins
     * @param position    追加位置
     * @throws StorageException
     */
    AppendResult appendSave(StorageType storageType, String key, InputStream ins, Long position) throws
            StorageException;


    /**
     * 异步回调方式,追加上传
     *
     * @param storageType
     * @param key
     * @param ins
     * @param position    追加位置
     * @throws StorageException
     */
    CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, InputStream ins, Long position)
            throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param ins
     * @param position
     * @param executor
     * @return
     * @throws StorageException
     */
    CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, InputStream ins, Long
            position, Executor executor) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param ins
     * @param position
     * @param params
     * @return
     * @throws StorageException
     */
    AppendResult appendSave(StorageType storageType, String key, InputStream ins, Long position,
                            StorageMetadata params) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param ins
     * @param position
     * @param params
     * @throws StorageException
     */
    CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, InputStream ins, Long position,
                                                    StorageMetadata params) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @param ins
     * @param position
     * @param params
     * @param executor
     * @return
     * @throws StorageException
     */
    CompletableFuture<AppendResult> appendSaveAsync(StorageType storageType, String key, InputStream ins, Long position,
                                                    StorageMetadata params, Executor executor) throws StorageException;

    /**
     * 删除
     *
     * @param storageType
     * @param key
     * @throws StorageException
     */
    void delete(StorageType storageType, String key) throws StorageException;

    /**
     * 异步回调方式删除
     *
     * @param storageType
     * @param key
     * @throws StorageException
     */
    CompletableFuture<StorageResult> deleteAsync(StorageType storageType, String key) throws
            StorageException;

    /**
     * @param storageType
     * @param key
     * @param executor
     * @return
     * @throws StorageException
     */
    CompletableFuture<StorageResult> deleteAsync(StorageType storageType, String key, Executor executor) throws
            StorageException;

    /**
     * 下载文件
     *
     * @param storageType
     * @param key
     * @return
     * @throws StorageException
     */
    StorageObject get(StorageType storageType, String key) throws StorageException;


    /**
     * 断点续传下载
     *
     * @param storageType
     * @param key
     * @param file
     * @param taskNum
     */
    DownloadResult resumeGet(StorageType storageType, String key, String file, int taskNum, long partSize);

    /**
     * @param storageType
     * @return
     * @throws StorageException
     */
    String getDomain(StorageType storageType) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @return
     * @throws StorageException
     */
    String generateUrl(StorageType storageType, String key) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @return
     * @throws StorageException
     */
    String generateUrl(StorageType storageType, String key, Date expiration) throws StorageException;

    /**
     * @param storageType
     * @param key
     * @return
     */
    Link getLink(StorageType storageType, String key);

    /**
     * @param storageType
     * @param key
     * @return
     */
    Link getLink(StorageType storageType, String key, Date expiration);

    /**
     * 资源类别
     */
    enum StorageType {
        /**
         * 验证码
         */
        CAPTCHA("captcha"),

        /**
         * 头像
         */
        AVATAR("avatar"),

        /**
         * 内容
         */
        CONTENT("content"),

        /**
         * 封面
         */
        COVER("cover"),

        /**
         *
         */
        DRM("drm"),

        /**
         *
         */
        APP("app"),

        /**
         *
         */
        USER_DATA("userData");

        private String type;

        StorageType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    class StorageResult implements Serializable {

        private StorageType storageType;
        private String key;

        public StorageResult() {
        }

        public StorageResult(StorageType storageType, String key) {
            this.storageType = storageType;
            this.key = key;
        }

        public StorageType getStorageType() {
            return storageType;
        }

        public void setStorageType(StorageType storageType) {
            this.storageType = storageType;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
