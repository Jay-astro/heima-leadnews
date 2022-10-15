package com.heima.test;


import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WemediaApplication.class)
public class aliyunTest {

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private MinIOFileStorageService storageService;

    /**
     * 文字审核
     * @throws Exception
     */
    @Test
    public void testTextScan() throws Exception {
        List<String> textList = new ArrayList<>();
        textList.add("小明");
        textList.add("如花");
        textList.add("法论功");
        Map result = greenTextScan.greeTextScan(textList);
        System.out.println("结果为："+result.get("suggestion"));
    }

    @Test
    public void testImageScan() throws Exception {
        List<byte[]> imageList = new ArrayList<>();
        //从MinIO下载图片
        byte[] image = storageService.downLoadFile("http://192.168.66.133:9000/leadnews/2022/09/28/f716ccc19bb74cc59ef3a4ec6997b705.png");
        imageList.add(image);

        Map result = greenImageScan.imageScan(imageList);
        System.out.println("结果为："+result.get("suggestion"));
    }
}
