package cn.bit.framework.utils.captcha;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.DropShadowGimpyRenderer;
import cn.apiclub.captcha.noise.CurvedLineNoiseProducer;
import cn.apiclub.captcha.noise.NoiseProducer;
import cn.apiclub.captcha.text.producer.*;
import cn.apiclub.captcha.text.renderer.DefaultWordRenderer;
import cn.apiclub.captcha.text.renderer.WordRenderer;
import cn.bit.framework.enums.CaptchaTextType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by terry on 2016/7/6.
 */
public class CaptchaImageUtils {


    private static final char[] DEFAULT_CHARS = new char[]{'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 'w', 'x', 'y',
            '2', '3', '4', '5', '6', '7', '8',};

    /**
     * 生成随机验证码图片
     *
     * @param type   验证码类别
     * @param width
     * @param height
     * @param noise
     * @param os
     * @param length
     * @return
     * @throws IOException
     */
    public static String generateCaptchaImg(
            CaptchaTextType type, int width, int height,
            int noise, OutputStream os, int length) throws IOException {
        Captcha.Builder builder = new Captcha.Builder(width, height);
        // 增加边框
        builder.addBorder();
        NoiseProducer nProd = new CurvedLineNoiseProducer(Color.gray, 0.5f);
        // 是否增加干扰线条
        //if (noise == 1)
        // builder.addNoise(nProd).addNoise().addNoise();
        for (int i = 0; i < noise; i++) {
            builder.addNoise(nProd);
        }
        // ----------------自定义字体大小-----------
        // 自定义设置字体颜色和大小 最简单的效果 多种字体随机显示
        List<Font> fontList = new ArrayList<Font>();
        fontList.add(new Font("Courier", Font.ITALIC, 40));
        fontList.add(new Font("Cambria", Font.PLAIN, 48));
        fontList.add(new Font("Arial", Font.ITALIC, 48));
        fontList.add(new Font("宋体", Font.HANGING_BASELINE, 40));// 可以设置斜体之类的
        fontList.add(new Font("宋体", Font.PLAIN, 40));

        // 加入多种颜色后会随机显示 字体空心
        List<Color> colorList = new ArrayList<Color>();
        colorList.add(Color.gray);
        colorList.add(new Color(152, 95, 24));
        colorList.add(new Color(9, 53, 178));
        colorList.add(new Color(7, 119, 63));
        colorList.add(new Color(76, 119, 8));
        colorList.add(new Color(148, 12, 102));
        colorList.add(new Color(89, 4, 178));
        colorList.add(new Color(89, 74, 178));
        colorList.add(new Color(148, 62, 102));
        colorList.add(new Color(76, 149, 58));
        colorList.add(new Color(7, 119, 93));
        colorList.add(new Color(49, 53, 178));
        colorList.add(new Color(152, 75, 94));
        colorList.add(new Color(212, 95, 24));
        colorList.add(new Color(9, 53, 238));
        colorList.add(new Color(7, 179, 63));
        colorList.add(new Color(76, 179, 8));
        colorList.add(new Color(208, 12, 102));
        colorList.add(new Color(89, 4, 238));
        colorList.add(new Color(89, 74, 238));
        colorList.add(new Color(208, 62, 102));
        colorList.add(new Color(76, 209, 58));
        colorList.add(new Color(7, 179, 93));
        colorList.add(new Color(49, 53, 238));
        colorList.add(new Color(212, 75, 94));

        DefaultWordRenderer cwr = new DefaultWordRenderer(colorList, fontList);
        //ColoredEdgesWordRenderer cwr= new ColoredEdgesWordRenderer(colorList,fontList);
        WordRenderer wr = cwr;
        // 增加文本，默认为5个随机字符.

        TextProducer tp = null;
        switch (type) {
            case DEFAULT:
                tp = new DefaultTextProducer(length, DEFAULT_CHARS);
                break;
            case NUMBER:
                tp = new NumbersAnswerProducer(length);
                break;
            case ARABIC:
                tp = new ArabicTextProducer(length);
                break;
            case CHINESE:
                tp = new ChineseTextProducer(length);
                break;
            case FIVE_LETTER_FIRSTNAME:
                tp = new ChineseTextProducer(length);
                break;
            default:
                tp = new DefaultTextProducer();
                break;
        }
        builder.addText(tp, wr);

        // --------------添加背景-------------
        // 设置背景渐进效果 以及颜色 form为开始颜色，to为结束颜色
        GradiatedBackgroundProducer gbp = new GradiatedBackgroundProducer();
        gbp.setFromColor(Color.BLUE);
        gbp.setToColor(Color.WHITE);

        // 无渐进效果，只是填充背景颜色
//      FlatColorBackgroundProducer fbp=new FlatColorBackgroundProducer(Color.white);
        // 加入网纹--一般不会用
        //SquigglesBackgroundProducer sbp=new SquigglesBackgroundProducer();
        // 没发现有什么用,可能就是默认的
        // TransparentBackgroundProducer tbp = new
        // TransparentBackgroundProducer();

        builder.addBackground(gbp);

        // ---------装饰字体---------------
        // 字体边框齿轮效果 默认是3
        //builder.gimp(new BlockGimpyRenderer(3));
        // 波纹渲染 相当于加粗
//      builder.gimp(new RippleGimpyRenderer());
        // 加网--第一个参数是横线颜色，第二个参数是竖线颜色
        //builder.gimp(new FishEyeGimpyRenderer(Color.gray,Color.darkGray));
        // 加入阴影效果 默认3，75
        builder.gimp(new DropShadowGimpyRenderer());
        //builder.
        Captcha captcha = builder.build();
        //System.err.println(captcha.getAnswer());
        ImageIO.write(captcha.getImage(), "JPEG", os);
        return captcha.getAnswer();
    }

    public static void main(String[] args) throws IOException {
        File f = new File("E:\\xxx.jpg");
        FileOutputStream fos = new FileOutputStream(f);

        String code = CaptchaImageUtils.generateCaptchaImg(CaptchaTextType.DEFAULT, 200, 70, 0, fos, 6);
        System.err.println(code);
    }
}
