package com.heima.common.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {
    private String accessKey;//
    private String secretKey;//账户密码
    private String endpoint;//MinIO连接地址   http://192.168.66.133:9000/

    private String bucket;//桶名称
    private String readPath;//访问文件的地址  http://192.168.66.133:9000/
}
