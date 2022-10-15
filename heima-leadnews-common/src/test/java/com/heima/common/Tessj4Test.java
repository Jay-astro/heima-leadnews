package com.heima.common;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Test;

import java.io.File;

public class Tessj4Test {
    @Test
    public void testScanImage() throws TesseractException {
        //创建对象
        ITesseract iTesseract = new Tesseract();
        //设置语言包
        iTesseract.setDatapath("D:\\Code\\tessdata");
        iTesseract.setLanguage("chi_sim");

        //扫描图片，获取文字
        String result = iTesseract.doOCR(new File("D:\\Code\\tessdata\\image-20210524161243572.png"));
        System.out.println(result);

    }
}
