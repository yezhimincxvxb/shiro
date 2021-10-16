package com.yzm.shiro08.config;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 生成验证码并存储在Session中
 */
@Slf4j
public class VerifyServlet extends HttpServlet {

    private static final long serialVersionUID = -5051097528828603895L;

    /**
     * 验证码图片的宽度。
     */
    private final int width = 100;

    /**
     * 验证码图片的高度。
     */
    private final int height = 30;

    /**
     * 验证码字符个数
     */
    private final int codeCount = 4;

    /**
     * 第一个字符的x轴值，因为后面的字符坐标依次递增，所以它们的x轴值是codeX的倍数
     */
    private int codeX;

    /**
     * codeY ,验证字符的y轴值，因为并行所以值一样
     */
    private int codeY;

    /**
     * 字体高度
     */
    private int fontHeight;

    /**
     * 干扰线数量
     */
    private final int interLine = 12;

    /**
     * codeSequence 表示字符允许出现的序列值
     */
    char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    /**
     * 初始化验证图片属性
     */
    @Override
    public void init() throws ServletException {
        //width-4 除去左右多余的位置，使验证码更加集中显示，减得越多越集中。
        //codeCount+1 等比分配显示的宽度，包括左右两边的空格
        codeX = (width - 4) / (codeCount + 1);
        //height - 10 集中显示验证码
        fontHeight = height - 10;
        codeY = height - 7;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        // 定义图像buffer
        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取Graphics对象,便于对图像进行各种绘制操作
        Graphics2D gd = buffImg.createGraphics();

        // 背景白色
        gd.setColor(Color.LIGHT_GRAY);
        gd.fillRect(0, 0, width, height);

        // 设置字体，字体的大小应该根据图片的高度来定。
        gd.setFont(new Font("Times New Roman", Font.PLAIN, fontHeight));

        // 画边框。
        gd.setColor(Color.BLACK);
        gd.drawRect(0, 0, width - 1, height - 1);

        // 随机产生干扰线，使图象中的认证码不易被其它程序探测到。
        gd.setColor(Color.green);
        Random random = new Random();
        for (int i = 0; i < interLine; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            gd.drawLine(x, y, x + xl, y + yl);
        }

        // randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
        StringBuilder randomCode = new StringBuilder();
        int red, green, blue;
        // 随机产生codeCount数字的验证码。
        for (int i = 0; i < codeCount; i++) {
            // 得到随机产生的验证码数字。
            String strRand = String.valueOf(codeSequence[random.nextInt(36)]);
            // 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);
            // 用随机产生的颜色将验证码绘制到图像中。
            gd.setColor(new Color(red, green, blue));
            gd.drawString(strRand, (i + 1) * codeX, codeY);
            // 将产生的四个随机数组合在一起。
            randomCode.append(strRand);
        }

        // 将四位数字的验证码保存到Session中。
        HttpSession session = request.getSession();
        session.setAttribute("validateCode", randomCode.toString());
        log.info("验证码：" + randomCode);

        // 禁止图像缓存。
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 将图像输出到Servlet输出流中。
        ServletOutputStream sos = response.getOutputStream();
        ImageIO.write(buffImg, "jpeg", sos);
        sos.close();
    }

}
