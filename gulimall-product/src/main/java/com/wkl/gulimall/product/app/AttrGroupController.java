package com.wkl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.wkl.gulimall.product.entity.AttrEntity;
import com.wkl.gulimall.product.service.AttrAttrgroupRelationService;
import com.wkl.gulimall.product.service.AttrService;
import com.wkl.gulimall.product.service.CategoryService;
import com.wkl.gulimall.product.vo.AttrGroupRelationVo;
import com.wkl.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wkl.gulimall.product.entity.AttrGroupEntity;
import com.wkl.gulimall.product.service.AttrGroupService;
import com.wkl.common.utils.PageUtils;
import com.wkl.common.utils.R;



/**
 * 属性分组
 *
 * @author wkl
 * @email 750583669@qq.com
 * @date 2021-01-02 16:26:13
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 获取分类下所有分组&关联属性
     * /product/attrgroup/{catelogId}/withattr
     * @param catelogId
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R attrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        //1.在分组表中查出attrid
        //2.在属性表中查出所有数据
        //3.组装成vo，并返回
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", vos);
    }

    /**
     * 属性分组查关联：/product/attrgroup/{attrgroupId}/attr/relation
     * 属性分组查关联，关联信息展示需要两个参数属性名和可选值，在AttrEntity中有
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        //页面需要属性名和可选值，根据attrgroupId在attr中查到需要的关联信息，需要这两个参数在AttrEntity类
        //创建这个方法
        List<AttrEntity> entities =  attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    /**
     * 属性分组查询尚未关联的属性 /product/attrgroup/{attrgroupId}/noattr/relation
     * 属性分组-关联-新建关联
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId){
        //查询属性分组尚未关联的属性
        PageUtils page =  attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }


    /**
     * 保存属性分组关联关系
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation")
    public R relationSave(@RequestBody List<AttrGroupRelationVo> vos){
        //创建方法，保存提交的所有关联
        attrAttrgroupRelationService.saveBachRelation(vos);
        return R.ok();
    }

    /**
     * 属性分组查关联删除/product/attrgroup/attr/relation/delete
     * 属性分组查关联删除，两个请求参数，[{"attrId":1,"attrGroupId":2}]，封装到VO
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody AttrGroupRelationVo[] vos){
        //创建方法，删除提交的所有关联
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 列表，查询指定id的信息
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R listById(@RequestParam Map<String, Object> params
            , @PathVariable("catelogId") Long catelogId){
        System.out.println("属性分组："+params+":"+catelogId);
        //这是以前的查询语句，Map params中是分页查询条件，现在需要新建查询方法
        //PageUtils page = attrGroupService.queryPage(params);

        //新建查询方法
        PageUtils page = attrGroupService.queryPageById(params, catelogId);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     * 添加查询路径的方法
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
        //根据id查基本信息
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		//取出id
        Long catelogId = attrGroup.getCatelogId();
        //查询完整路径，使用三级分类service查
        Long[] path = categoryService.findCatelogPath(catelogId);
        //设置完整路径
        attrGroup.setCatelogPath(path);


        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
