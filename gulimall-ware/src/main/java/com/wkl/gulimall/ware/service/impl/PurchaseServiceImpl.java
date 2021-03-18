package com.wkl.gulimall.ware.service.impl;

import com.wkl.common.constant.WareConstant;
import com.wkl.common.utils.R;
import com.wkl.gulimall.ware.entity.PurchaseDetailEntity;
import com.wkl.gulimall.ware.service.PurchaseDetailService;
import com.wkl.gulimall.ware.service.WareSkuService;
import com.wkl.gulimall.ware.vo.MergeVo;
import com.wkl.gulimall.ware.vo.PurchaseDoneVo;
import com.wkl.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.Query;

import com.wkl.gulimall.ware.dao.PurchaseDao;
import com.wkl.gulimall.ware.entity.PurchaseEntity;
import com.wkl.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.xml.soap.DetailEntry;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService detailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 未领取的采购单
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        //状态是新增、已分配的可以被查询
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );
        return new PageUtils(page);
    }

    /**
     * 合并多个采购需求为一个采购单
     * 事务 方法
     * @param mergeVo
     */
    @Transactional
    @Override
    public R mergePurchase(MergeVo mergeVo) {

        //TODO 确认采购单状态是0,1才可以合并
        //先过滤，看看里面是否存在正在采购的需求
        List<Long> details = mergeVo.getItems();
        List<Long> detailStatus = details.stream().filter((item) -> {
            //确认采购单状态是0,1才可以合并
            System.out.println("采购需求" + item);
            PurchaseDetailEntity byId = detailService.getById(item);
            if (byId.getStatus() == 0 || byId.getStatus() == 1) {
                //如果符合就通过
                return true;
            }
            //不符合就跳过
            return false;
        }).collect(Collectors.toList());

        //除非都是新建状态，，否则全部不能合并，全部通过说明与原始列表长度相同
        if (detailStatus.size() == details.size()){
            //0.取出采购单，查看是否已经存在这个采购单，有则合并，没有就新建一个采购单
            Long purchaseId = mergeVo.getPurchaseId();
            if(purchaseId == null){
                //0.确认采购单状态是0,1才可以合并
                //1、新建一个
                PurchaseEntity purchaseEntity = new PurchaseEntity();
                //设置新的采购单状态，新建采购单状态为0，使用自定义的枚举类
                purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
                purchaseEntity.setCreateTime(new Date());
                purchaseEntity.setUpdateTime(new Date());
                this.save(purchaseEntity);
                //已经有了采购单，取出id
                purchaseId = purchaseEntity.getId();
            }

            //2.修改采购需求的信息，状态改为已分配
            //List<Long> items = mergeVo.getItems();
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> collect = detailStatus.stream().map(i -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                //设置采购需求id
                detailEntity.setId(i);
                //设置采购需求分配采购单id
                detailEntity.setPurchaseId(finalPurchaseId);
                //设置采购需求状态
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            //批量保存已经修改的采购需求
            detailService.updateBatchById(collect);

            //3.修改合并采购单时的时间，只修改时间这一个属性
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
            return R.ok();
        }
        return R.error().put("msg","无法合并");

    }

    /**
     * 采购人员领取采购单
     * @param ids
     * @return
     */
    @Override
    public void received(List<Long> ids) {

        //1、确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            //查出当前采购单
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            //过滤提交的采购单状态，0、1状态才能领取
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item->{
            //设置采购单状态和时间
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2、改变采购单的状态
        this.updateBatchById(collect);

        //3、改变采购需求项的状态
        collect.forEach((item)->{
            //获取采购需求中被分配指定采购单id的采购需求
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());
            //设置这些被分配的采购需求的状态
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity entity1 = new PurchaseDetailEntity();
                entity1.setId(entity.getId());
                entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity1;
            }).collect(Collectors.toList());
            //更新采购需求
            detailService.updateBatchById(detailEntities);
        });
    }

    /**
     * 采购人员完成采购
     * @param doneVo
     */
    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        //获取采购单id，在修改采购单之前必须保证采购需求都无异常，否则不能正确修改采购单
        Long id = doneVo.getId();

        //2、改变采购项的状态
        Boolean flag = true;
        //获取请求中的采购需求
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        //待更新采购需求列表
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();

            //判断采购需求状态
            if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                //采购需求异常，设置异常状态
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else{
                //如果采购需求状态正确，可以完成采购
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3、将成功采购的进行入库
                //查出采购需求，取出skuId，仓库Id，采购数量，进行入库
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                //调用仓库的服务，给库存添加采购的数量
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            //更新采购需求属性，放进列表
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        //批量更新采购需求
        detailService.updateBatchById(updates);

        //1、改变采购单状态，只有前面的采购需求都完成才能改变采购单
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode():WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}