package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.PageResponseResult;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.ThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Service
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private MinIOFileStorageService storageService;

    @Override
    public ResponseResult uploadPicture(MultipartFile file) {
        //参数校验
        if (file==null){
            throw new LeadNewsException(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //获取登录用户信息
        WmUser wmUser = (WmUser) ThreadLocalUtils.get();
        if (wmUser == null){
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }
        try {
            //上传到minio
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            //获取文件后缀
            String originalFilename = file.getOriginalFilename();
            String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = uuid + extName;
            String url = storageService.uploadimgFile(null, fileName, file.getInputStream());

            //写入db
            WmMaterial wmMaterial = new WmMaterial();
            wmMaterial.setUserId(wmUser.getId());
            wmMaterial.setUrl(url);
            wmMaterial.setCreatedTime(new Date());
            wmMaterial.setIsCollection((short)0);
            wmMaterial.setType((short)0);
            save(wmMaterial);

            //返回素材信息
            return ResponseResult.okResult(wmMaterial);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PageResponseResult findList(WmMaterialDto dto) {
        //处理参数
        dto.checkParam();

        WmUser wmUser = (WmUser) ThreadLocalUtils.get();
        if (wmUser == null){
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }

        //分页参数
        IPage<WmMaterial> iPage = new Page<>(dto.getPage(),dto.getSize());

        //条件参数
        QueryWrapper<WmMaterial> queryWrapper = new QueryWrapper<>();
        //判断当前登录用户
        queryWrapper.eq("user_id",wmUser.getId());

        //是否收藏
        if (dto.getIsCollection() != null && dto.getIsCollection() == 1){
            queryWrapper.eq("is_collection",dto.getIsCollection());
        }

        //排序
        queryWrapper.orderByDesc("created_time");


        //分页查询
        iPage = page(iPage,queryWrapper);

        //封装数据
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)iPage.getTotal());
        pageResponseResult.setData(iPage.getRecords());
        pageResponseResult.setCode(200);
        pageResponseResult.setErrorMessage("查询成功");

        return pageResponseResult;

    }
}
