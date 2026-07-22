package com.example.myatomsbackend.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SkillMarketplaceService {
    public record Skill(String id, String name, String description, String category,
                        String agent, List<String> keywords, String instructions,
                        boolean featured) {}

    private final Map<String, Skill> catalog = new LinkedHashMap<>();
    private final Set<String> installed = ConcurrentHashMap.newKeySet();

    public SkillMarketplaceService() {
        add(new Skill("landing-page", "高质量落地页", "生成有品牌感、强转化且响应式的产品官网。", "网页", "Alex",
                List.of("落地页", "官网", "产品介绍", "landing page"),
                "生成可直接预览的完整单文件 HTML，内联 CSS 和 JavaScript。包含导航、Hero、产品价值、功能、社会证明、价格或 CTA、FAQ 与页脚。必须响应式、视觉有辨识度、按钮可点击，禁止占位文案和不可用交互；只输出完整 HTML 代码。", true));
        add(new Skill("saas-dashboard", "SaaS 产品界面", "生成专业的后台、工作台和管理系统 Demo。", "应用", "Alex",
                List.of("SaaS", "后台", "管理系统", "工作台", "dashboard"),
                "生成完整单文件 HTML SaaS Demo，包含侧边栏、顶部工具栏、指标卡、数据列表、筛选、分页、弹窗和空状态。导航切换、搜索、筛选、增删改必须真实可用，数据使用 localStorage 持久化；只输出完整 HTML。", true));
        add(new Skill("mobile-web-app", "移动端 Web App", "生成适合手机操作的 H5/PWA 产品原型。", "移动端", "Alex",
                List.of("移动端", "H5", "手机应用", "PWA", "小程序界面"),
                "生成移动优先的完整单文件 HTML，包含安全区适配、底部导航、触控友好的按钮、列表与详情、弹层和反馈状态。页面在 375px 宽度下可直接使用，同时兼容桌面预览；交互闭环并使用 localStorage 保存数据。", true));
        add(new Skill("ecommerce-demo", "电商商城 Demo", "生成商品、购物车和结算流程完整的商城原型。", "电商", "Alex",
                List.of("商城", "电商", "商品", "购物车", "结算"),
                "生成完整单文件 HTML 商城，包含商品分类、搜索、商品卡、详情弹窗、规格选择、购物车数量增减、金额计算和结算反馈。所有操作必须可点击且状态同步，购物车使用 localStorage 持久化，使用真实感示例数据而非空占位。", true));
        add(new Skill("crud-app", "表单与 CRUD 应用", "生成可增删改查、搜索筛选和持久化的业务工具。", "应用", "Alex",
                List.of("CRUD", "增删改查", "表单", "列表管理", "记账", "待办"),
                "生成完整单文件 HTML 业务应用。包含表单校验、新增、编辑、删除确认、搜索、筛选、排序、统计和空状态；所有数据存入 localStorage，刷新不丢失。用页面内 Toast 或提示反馈，禁止 alert、prompt 和只有外观没有逻辑的按钮。", true));
        add(new Skill("auth-user-center", "登录与用户中心", "生成登录、注册、资料和权限界面 Demo。", "账号", "Alex",
                List.of("登录", "注册", "用户中心", "个人资料", "权限"),
                "生成完整单文件 HTML，包含登录/注册切换、字段校验、密码显隐、记住登录、忘记密码反馈、用户资料编辑和退出登录。使用 localStorage 模拟会话，不接收或展示真实密钥；所有状态和错误在页面内反馈。", false));
        add(new Skill("data-dashboard", "数据可视化看板", "生成指标卡、趋势图和多维筛选的数据大屏。", "数据", "Alex",
                List.of("数据看板", "数据大屏", "图表", "可视化", "指标"),
                "生成可直接预览的完整 HTML 数据看板，包含指标卡、趋势、排行、构成、时间范围与维度筛选。优先使用原生 SVG 或 Canvas 绘图，禁止依赖无法加载的外部图表库；筛选必须联动数据与图表，并展示口径、单位和更新时间。", true));
        add(new Skill("visual-polish", "动效与视觉升级", "把普通页面升级成有辨识度的高完成度产品界面。", "设计", "Alex",
                List.of("美化", "视觉升级", "动效", "UI", "设计优化"),
                "在不破坏功能的前提下生成或重构完整 HTML。建立统一的字体、颜色、间距和组件体系，加入克制的入场与交互反馈，支持 prefers-reduced-motion。装饰层不得阻挡点击，移动端必须可用，避免白底紫色渐变和模板化三卡片布局。", false));
        add(new Skill("spring-api", "Spring Boot API", "为产品 Demo 生成 Controller、Service、DTO 和接口。", "后端", "Alex + Bob",
                List.of("Spring Boot", "后端接口", "REST API", "Controller", "Java 后端"),
                "输出可运行的 Spring Boot 3/4 Java 17 项目代码结构，至少包含 Controller、Service、DTO、统一异常处理、参数校验、CORS 与示例请求。接口围绕用户的 Demo 核心流程，不生成无关模块；密钥使用环境变量，禁止硬编码。用清晰代码块标明每个文件路径。", false));
        add(new Skill("demo-quality-check", "Demo 测试与修复", "检查并修复按钮、表单、脚本和响应式问题。", "质量", "Alex + David",
                List.of("测试修复", "检查 Demo", "按钮没反应", "页面报错", "响应式"),
                "逐项检查页面加载、导航、按钮、表单校验、增删改、localStorage、弹窗、空状态、移动端和控制台错误。直接给出修复后的完整可运行 HTML，而不是只列问题；确保所有交互元素有真实事件且不被装饰层遮挡。", false));
    }

    private void add(Skill skill) { catalog.put(skill.id(), skill); }

    public List<Map<String, Object>> list() {
        return catalog.values().stream().map(skill -> {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("id", skill.id()); value.put("name", skill.name());
            value.put("description", skill.description()); value.put("category", skill.category());
            value.put("agent", skill.agent()); value.put("keywords", skill.keywords());
            value.put("featured", skill.featured()); value.put("installed", installed.contains(skill.id()));
            return value;
        }).toList();
    }

    public Skill install(String id) {
        Skill skill = require(id); installed.add(id); return skill;
    }
    public void uninstall(String id) { installed.remove(id); }
    public List<Skill> resolve(List<String> ids, String prompt) {
        Set<String> selected = new LinkedHashSet<>(ids == null ? List.of() : ids);
        String lower = prompt == null ? "" : prompt.toLowerCase();
        for (String id : installed) {
            Skill skill = catalog.get(id);
            if (skill != null && skill.keywords().stream().anyMatch(k -> lower.contains(k.toLowerCase()))) selected.add(id);
        }
        return selected.stream().filter(installed::contains).map(catalog::get).filter(Objects::nonNull).toList();
    }
    private Skill require(String id) {
        Skill skill = catalog.get(id);
        if (skill == null) throw new NoSuchElementException("Skill 不存在");
        return skill;
    }
}
