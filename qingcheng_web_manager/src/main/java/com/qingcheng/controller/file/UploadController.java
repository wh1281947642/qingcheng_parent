package com.qingcheng.controller.file;

import com.aliyun.oss.OSSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * <p>
 * <code>UploadController</code>
 * </p>
 * 文件上传
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/03/16 15:05
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private HttpServletRequest request;

    /**
     * 上传至本地
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/16 15:06
     * @param
     * @return
     */
    @PostMapping("/native")
    public String nativeUpload(@RequestParam("file") MultipartFile file){
        System.out.println("native......");
        //获取到文件的绝对路径
        String path=  request.getSession().getServletContext().getRealPath("img");
        //获取到文件名
        String filePath= path +"/"+file.getOriginalFilename();
        //创建文件
        File desFile=new File(filePath);
        //判断img目录是否存在
        if(!desFile.getParentFile().exists()){
            desFile.mkdirs();
        }
        try {
            file.transferTo(desFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "http://localhost:9101/img/"+file.getOriginalFilename();
    }

    @Autowired
    private OSSClient ossClient;

    /**
     * 阿里云OSS
     * @description
     * @author huiwang45@iflytek.com
     * @date 2020/03/16 17:38
     * @param folder 目录
     * @return
     */
    @PostMapping("/oss")
    public String ossUpload(@RequestParam("file") MultipartFile file,String folder){
        String bucketName="wh-qingchengdianshang";
        //原始文件名，会重名
        //String originalFilename = file.getOriginalFilename();
        //保证每一个上传文件不重名
        String fileName = folder+"/"+ UUID.randomUUID()+"-"+ file.getOriginalFilename();
        try {
            ossClient.putObject(bucketName,fileName,file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "https://"+bucketName+".oss-cn-beijing.aliyuncs.com/"+fileName;
    }
}
