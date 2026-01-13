<template>
  <div class="theme-sample-page">
    <div class="sample-header">
      <div class="title">THEME_SAMPLE // 主题样式预览</div>
      <div class="subtitle">RETRO_FUTURISM_UI_KIT_V1.0</div>
    </div>

    <div class="sample-content">
      <!-- 1. 基础排版与色彩 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>01. TYPOGRAPHY & COLORS // 排版与色彩</span>
        </div>
        <div class="color-palette">
          <div class="color-box" style="background: var(--space-black); color: #fff;">
            BLACK<br>#181818
          </div>
          <div class="color-box" style="background: var(--lunar-cream); color: #000;">
            CREAM<br>#F5EDDC
          </div>
          <div class="color-box" style="background: var(--mission-rust); color: #fff;">
            RUST<br>#B2653B
          </div>
          <div class="color-box" style="background: var(--avionics-mustard); color: #fff;">
            MUSTARD<br>#BA8530
          </div>
          <div class="color-box" style="background: var(--oxidized-sage); color: #fff;">
            SAGE<br>#5C7F71
          </div>
          <div class="color-box" style="background: var(--alarm-burgundy); color: #fff;">
            BURGUNDY<br>#802520
          </div>
        </div>
        <div class="typography-demo">
          <h1>H1. THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG.</h1>
          <h2>H2. 0123456789 // SYSTEM READY.</h2>
          <p>Paragraph. This is a sample text block demonstrating the Courier Prime font rendering. The aesthetics should feel like a retro terminal or a printed report from the 80s.</p>
        </div>
      </el-card>

      <!-- 2. 按钮 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>02. BUTTONS // 按钮交互</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="24">
            <p class="section-label">TYPES:</p>
            <el-button>Default</el-button>
            <el-button type="primary">Primary</el-button>
            <el-button type="success">Success</el-button>
            <el-button type="warning">Warning</el-button>
            <el-button type="danger">Danger</el-button>
            <el-button type="info">Info</el-button>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="24">
             <p class="section-label">SIZES & STATES:</p>
            <el-button type="primary" size="medium">Medium</el-button>
            <el-button type="primary" size="small">Small</el-button>
            <el-button type="primary" size="mini">Mini</el-button>
            <el-button type="primary" disabled>Disabled</el-button>
            <el-button type="primary" loading>Loading</el-button>
            <el-button type="primary" icon="el-icon-search">Search</el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 2-B. 按钮样式单选 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>02-B. RADIO BUTTONS // 按钮样式单选</span>
        </div>
        <div class="demo-block">
          <p class="section-label">SIZES:</p>
          <div style="margin-bottom: 20px;">
            <el-radio-group v-model="radioGroup">
              <el-radio-button label="Shanghai">上海</el-radio-button>
              <el-radio-button label="Beijing">北京</el-radio-button>
              <el-radio-button label="Guangzhou">广州</el-radio-button>
              <el-radio-button label="Shenzhen">深圳</el-radio-button>
            </el-radio-group>
          </div>
          <div style="margin-bottom: 20px;">
            <el-radio-group v-model="radioGroup" size="medium">
              <el-radio-button label="Shanghai">上海</el-radio-button>
              <el-radio-button label="Beijing">北京</el-radio-button>
              <el-radio-button label="Guangzhou">广州</el-radio-button>
              <el-radio-button label="Shenzhen">深圳</el-radio-button>
            </el-radio-group>
          </div>
          <div style="margin-bottom: 20px;">
            <el-radio-group v-model="radioGroup" size="small">
              <el-radio-button label="Shanghai">上海</el-radio-button>
              <el-radio-button label="Beijing">北京</el-radio-button>
              <el-radio-button label="Guangzhou">广州</el-radio-button>
              <el-radio-button label="Shenzhen">深圳</el-radio-button>
            </el-radio-group>
          </div>
          <div style="margin-bottom: 20px;">
            <el-radio-group v-model="radioGroup" size="mini">
              <el-radio-button label="Shanghai">上海</el-radio-button>
              <el-radio-button label="Beijing">北京</el-radio-button>
              <el-radio-button label="Guangzhou">广州</el-radio-button>
              <el-radio-button label="Shenzhen">深圳</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </el-card>

      <!-- 3. 表单 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>03. FORMS // 表单输入</span>
        </div>
        <el-form :model="form" label-width="120px">
          <el-form-item label="Input Text">
            <el-input v-model="form.input" placeholder="请输入内容..."></el-input>
          </el-form-item>
          <el-form-item label="Password">
            <el-input placeholder="请输入密码" v-model="form.password" show-password></el-input>
          </el-form-item>
          <el-form-item label="Composite">
            <el-input placeholder="请输入网址" v-model="form.website">
              <template slot="prepend">Http://</template>
              <template slot="append">.com</template>
            </el-input>
          </el-form-item>
          <el-form-item label="Select Option">
            <el-select v-model="form.select" placeholder="请选择活动区域">
              <el-option label="Zone One" value="shanghai"></el-option>
              <el-option label="Zone Two" value="beijing"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="Cascader">
            <el-cascader v-model="form.cascader" :options="cascaderOptions" placeholder="请选择层级..."></el-cascader>
          </el-form-item>
          <el-form-item label="DatePicker">
            <el-date-picker v-model="form.date" type="date" placeholder="选择日期"></el-date-picker>
          </el-form-item>
          <el-form-item label="Slider">
            <span class="section-label" style="font-size: 12px; display: block; margin-bottom: 5px;">STANDARD:</span>
            <el-slider v-model="form.sliderVal1"></el-slider>
            <span class="section-label" style="font-size: 12px; display: block; margin-top: 15px; margin-bottom: 5px;">DISCRETE (STEP=10):</span>
            <el-slider v-model="form.sliderVal2" :step="10" show-stops></el-slider>
          </el-form-item>
          <el-form-item label="Counter">
            <el-input-number v-model="form.num" :min="1" :max="10" label="描述文字"></el-input-number>
          </el-form-item>
          <el-form-item label="Switch">
            <el-switch v-model="form.switch"></el-switch>
          </el-form-item>
          <el-form-item label="Checkbox">
            <el-checkbox-group v-model="form.check">
              <el-checkbox label="Option A"></el-checkbox>
              <el-checkbox label="Option B"></el-checkbox>
              <el-checkbox label="Option C" disabled></el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="Radio">
            <el-radio-group v-model="form.radio">
              <el-radio label="1">Radio A</el-radio>
              <el-radio label="2">Radio B</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="Textarea">
            <el-input type="textarea" v-model="form.desc" placeholder="请输入详细描述..."></el-input>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 3-B. 输入框尺寸 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>03-B. INPUT SIZES // 输入框尺寸</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="demo-input-size">
              <p class="section-label">DEFAULT (40px):</p>
              <el-input placeholder="Default Size" v-model="form.input"></el-input>
              <div style="margin: 15px 0;"></div>
              
              <p class="section-label">MEDIUM (36px):</p>
              <el-input size="medium" placeholder="Medium Size" v-model="form.input"></el-input>
              <div style="margin: 15px 0;"></div>
              
              <p class="section-label">SMALL (32px):</p>
              <el-input size="small" placeholder="Small Size" v-model="form.input"></el-input>
              <div style="margin: 15px 0;"></div>
              
              <p class="section-label">MINI (28px):</p>
              <el-input size="mini" placeholder="Mini Size" v-model="form.input"></el-input>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 3-C. 表单验证 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>03-C. FORM VALIDATION // 表单验证</span>
        </div>
        <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="120px" class="demo-ruleForm">
          <el-form-item label="Activity Name" prop="name">
            <el-input v-model="ruleForm.name"></el-input>
          </el-form-item>
          <el-form-item label="Activity Zone" prop="region">
            <el-select v-model="ruleForm.region" placeholder="Select Activity Zone">
              <el-option label="Zone one" value="shanghai"></el-option>
              <el-option label="Zone two" value="beijing"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitForm('ruleForm')">CREATE</el-button>
            <el-button @click="resetForm('ruleForm')">RESET</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 4. 标签与提示 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>04. FEEDBACK // 状态反馈</span>
        </div>
        <div class="demo-block">
          <p class="section-label">TAGS:</p>
          <el-tag>Tag One</el-tag>
          <el-tag type="success">Tag Two</el-tag>
          <el-tag type="info">Tag Three</el-tag>
          <el-tag type="warning">Tag Four</el-tag>
          <el-tag type="danger">Tag Five</el-tag>
        </div>
        <div class="demo-block">
          <p class="section-label">TAG SIZES:</p>
          <el-tag>Default</el-tag>
          <el-tag size="medium">Medium</el-tag>
          <el-tag size="small">Small</el-tag>
          <el-tag size="mini">Mini</el-tag>
        </div>
        <div class="demo-block">
          <p class="section-label">REMOVABLE TAGS:</p>
          <el-tag closable>Tag One</el-tag>
          <el-tag type="success" closable>Tag Two</el-tag>
          <el-tag type="info" closable>Tag Three</el-tag>
          <el-tag type="warning" closable>Tag Four</el-tag>
          <el-tag type="danger" closable>Tag Five</el-tag>
        </div>
        <div class="demo-block">
          <p class="section-label">DYNAMIC TAGS:</p>
          <el-tag
            :key="tag"
            v-for="tag in dynamicTags"
            closable
            :disable-transitions="false"
            @close="handleClose(tag)">
            {{tag}}
          </el-tag>
          <el-input
            class="input-new-tag"
            v-if="inputVisible"
            v-model="inputValue"
            ref="saveTagInput"
            size="small"
            @keyup.enter.native="handleInputConfirm"
            @blur="handleInputConfirm"
          >
          </el-input>
          <el-button v-else class="button-new-tag" size="small" @click="showInput">+ New Tag</el-button>
        </div>
        <div class="demo-block">
          <p class="section-label">ALERTS:</p>
           <el-alert title="SUCCESS ALERT" type="success" show-icon style="margin-bottom: 10px;"></el-alert>
           <el-alert title="INFO ALERT" type="info" show-icon style="margin-bottom: 10px;"></el-alert>
           <el-alert title="WARNING ALERT" type="warning" show-icon style="margin-bottom: 10px;"></el-alert>
           <el-alert title="ERROR ALERT" type="error" show-icon style="margin-bottom: 10px;"></el-alert>
           
           <div style="margin-top: 20px;">
             <el-alert
                title="SYSTEM UPDATE"
                type="success"
                description="The system has been successfully updated to version 2.0. All modules are functioning within normal parameters."
                show-icon
                style="margin-bottom: 10px;">
              </el-alert>
              <el-alert
                title="CRITICAL ERROR"
                type="error"
                description="Connection to the mainframe has been lost. Please contact the administrator immediately."
                show-icon>
              </el-alert>
           </div>
        </div>
        <div class="demo-block">
          <p class="section-label">MESSAGE BOX:</p>
          <el-button @click="openMsgSuccess">Success Msg</el-button>
          <el-button @click="openMsgWarning">Warning Msg</el-button>
          <el-button @click="openDialog">Open Dialog</el-button>
        </div>
        <div class="demo-block">
          <p class="section-label">NOTIFICATIONS:</p>
          <el-button @click="openNotificationSuccess">Success Notify</el-button>
          <el-button @click="openNotificationWarning">Warning Notify</el-button>
          <el-button @click="openNotificationError">Error Notify</el-button>
          <el-button @click="openNotificationInfo">Info Notify</el-button>
        </div>
      </el-card>

      <!-- 4-B. 进度条 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>04-B. PROGRESS // 进度条</span>
        </div>
        
        <div class="demo-block">
          <p class="section-label">LINEAR PROGRESS:</p>
          <div style="margin-bottom: 10px;"><el-progress :percentage="50" :stroke-width="18" :text-inside="true"></el-progress></div>
          <div style="margin-bottom: 10px;"><el-progress :percentage="100" status="success" :stroke-width="18" :text-inside="true"></el-progress></div>
          <div style="margin-bottom: 10px;"><el-progress :percentage="70" status="warning" :stroke-width="18" :text-inside="true"></el-progress></div>
          <div style="margin-bottom: 10px;"><el-progress :percentage="50" status="exception" :stroke-width="18" :text-inside="true"></el-progress></div>
        </div>

        <div class="demo-block">
           <p class="section-label">CIRCULAR PROGRESS:</p>
           <el-row :gutter="20">
             <el-col :span="6">
               <el-progress type="circle" :percentage="25"></el-progress>
             </el-col>
             <el-col :span="6">
               <el-progress type="circle" :percentage="100" status="success"></el-progress>
             </el-col>
             <el-col :span="6">
               <el-progress type="circle" :percentage="70" status="warning"></el-progress>
             </el-col>
             <el-col :span="6">
               <el-progress type="circle" :percentage="50" status="exception"></el-progress>
             </el-col>
           </el-row>
        </div>

        <div class="demo-block">
           <p class="section-label">DASHBOARD PROGRESS:</p>
           <el-row :gutter="20">
             <el-col :span="6">
               <el-progress type="dashboard" :percentage="80"></el-progress>
             </el-col>
             <el-col :span="6">
               <el-progress type="dashboard" :percentage="100" status="success"></el-progress>
             </el-col>
           </el-row>
        </div>
      </el-card>

      <!-- 4-C. 加载状态 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>04-C. LOADING // 加载状态</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="12">
            <p class="section-label">ELEMENT LOADING:</p>
            <div
              v-loading="loading"
              element-loading-text="LOADING DATA..."
              element-loading-spinner="el-icon-loading"
              element-loading-background="rgba(245, 237, 220, 0.8)"
              style="width: 100%; height: 200px; border: 2px solid var(--color-black); display: flex; align-items: center; justify-content: center;">
              <div style="text-align: center;">
                <p>CONTENT AREA</p>
                <p style="font-size: 12px; color: #666;">(Toggle switch below to control loading)</p>
              </div>
            </div>
            <div style="margin-top: 15px;">
               <el-switch v-model="loading" active-text="Loading Active" inactive-text="Loading Inactive"></el-switch>
            </div>
          </el-col>
          <el-col :span="12">
            <p class="section-label">FULL SCREEN LOADING:</p>
            <el-button
              type="primary"
              @click="openFullScreen1"
              v-loading.fullscreen.lock="fullscreenLoading">
              FULL SCREEN (3 SEC)
            </el-button>
            <el-button
              type="danger"
              @click="openFullScreen2">
              SERVICE LOADING (CUSTOM)
            </el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 5. 表格 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>05. DATA TABLE // 数据列表</span>
        </div>
        <el-table :data="tableData" style="width: 100%" border>
          <el-table-column prop="date" label="DATE" width="180"></el-table-column>
          <el-table-column prop="name" label="NAME" width="180"></el-table-column>
          <el-table-column prop="address" label="ADDRESS"></el-table-column>
          <el-table-column label="STATUS" width="100">
             <template slot-scope="scope">
               <el-tag size="medium" :type="scope.row.tagType">{{ scope.row.status }}</el-tag>
             </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 5-B. 分页 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>05-B. PAGINATION // 分页导航</span>
        </div>
        <div class="demo-block">
          <p class="section-label">BASIC PAGINATION:</p>
          <el-pagination
            layout="prev, pager, next"
            :total="50">
          </el-pagination>
        </div>
        <div class="demo-block">
          <p class="section-label">BACKGROUND PAGINATION:</p>
          <el-pagination
            background
            layout="prev, pager, next"
            :total="1000">
          </el-pagination>
        </div>
        <div class="demo-block">
          <p class="section-label">SMALL PAGINATION:</p>
          <el-pagination
            small
            layout="prev, pager, next"
            :total="50">
          </el-pagination>
        </div>
        <div class="demo-block">
          <p class="section-label">COMPLETE PAGINATION:</p>
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 30, 40]"
            :page-size="10"
            layout="total, sizes, prev, pager, next, jumper"
            :total="400">
          </el-pagination>
        </div>
      </el-card>
      
      <!-- 5-C. 空状态 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>05-C. EMPTY STATE // 空状态</span>
        </div>
        <div class="demo-block">
          <p class="section-label">BASIC EMPTY:</p>
          <el-empty description="NO DATA // 暂无数据"></el-empty>
        </div>
        <div class="demo-block">
           <p class="section-label">CUSTOM EMPTY:</p>
           <el-empty image="https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png" description="SYSTEM OFFLINE // 系统离线">
             <el-button type="primary">RETRY CONNECTION</el-button>
           </el-empty>
        </div>
      </el-card>
      
      <!-- 5-D. 结果反馈 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>05-D. RESULT // 结果反馈</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-result icon="success" title="SUCCESS" subTitle="Operation completed.">
              <template slot="extra">
                <el-button type="primary" size="medium">BACK</el-button>
              </template>
            </el-result>
          </el-col>
          <el-col :span="6">
            <el-result icon="warning" title="WARNING" subTitle="Please check your network.">
              <template slot="extra">
                <el-button type="primary" size="medium">RETRY</el-button>
              </template>
            </el-result>
          </el-col>
          <el-col :span="6">
            <el-result icon="error" title="ERROR" subTitle="System malfunction detected.">
              <template slot="extra">
                <el-button type="primary" size="medium">REPORT</el-button>
              </template>
            </el-result>
          </el-col>
          <el-col :span="6">
            <el-result icon="info" title="INFO" subTitle="Waiting for user input.">
              <template slot="extra">
                <el-button type="primary" size="medium">OK</el-button>
              </template>
            </el-result>
          </el-col>
        </el-row>
        
        <div style="margin: 30px 0; border-bottom: 1px dashed var(--color-black);"></div>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="section-label">CUSTOM ICON SLOT:</div>
            <el-result title="LOADING" subTitle="Processing data...">
              <template slot="icon">
                <div class="custom-loading-icon">
                  <i class="el-icon-loading"></i>
                </div>
              </template>
              <template slot="extra">
                <el-button type="primary" loading>PROCESSING</el-button>
              </template>
            </el-result>
          </el-col>
          <el-col :span="12">
            <div class="section-label">CUSTOM 404:</div>
             <el-result title="404" subTitle="PAGE NOT FOUND // 页面丢失">
               <template slot="icon">
                  <div class="custom-text-icon">?</div>
               </template>
               <template slot="extra">
                 <el-button type="primary" size="medium">HOME</el-button>
               </template>
             </el-result>
          </el-col>
        </el-row>
      </el-card>
      
       <!-- 6. 选项卡 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>06. TABS // 选项卡</span>
        </div>
        <el-tabs type="border-card">
          <el-tab-pane label="SYSTEM_LOG">
            System initialization... <br>
            Loading core modules... [OK] <br>
            Checking network... [OK]
          </el-tab-pane>
          <el-tab-pane label="USER_CONFIG">User configuration panel placeholder.</el-tab-pane>
          <el-tab-pane label="TASKS">Active tasks list.</el-tab-pane>
        </el-tabs>
      </el-card>

      <!-- 7. 背景模板 -->
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>07. BACKGROUND TEMPLATES // 背景模板</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="bg-preview-container">
              <div class="bg-preview theme-bg-lunar-grid">
                <span class="preview-label">LUNAR GRID (DEFAULT)</span>
              </div>
              <el-button style="margin-top: 10px; width: 100%;" @click="setGlobalBg('theme-bg-lunar-grid')">APPLY LUNAR GRID</el-button>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="bg-preview-container">
              <div class="bg-preview theme-bg-space-dots">
                <span class="preview-label" style="color: #fff; text-shadow: 0 0 5px #000;">SPACE DOTS</span>
              </div>
              <el-button style="margin-top: 10px; width: 100%;" @click="setGlobalBg('theme-bg-space-dots')">APPLY SPACE DOTS</el-button>
            </div>
          </el-col>
        </el-row>
      </el-card>

    </div>

    <!-- Dialog Demo -->
    <el-dialog title="SYSTEM_NOTICE // 系统通知" :visible.sync="dialogVisible" width="30%">
      <span>This is a standard dialog box demonstrating the border and shadow styles.</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">CANCEL</el-button>
        <el-button type="primary" @click="dialogVisible = false">CONFIRM</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ThemeSampleView',
  data() {
    return {
      form: {
        input: '',
        password: '',
        website: '',
        select: '',
        cascader: [],
        date: '',
        sliderVal1: 30,
        sliderVal2: 50,
        num: 1,
        switch: true,
        check: ['Option A'],
        radio: '1',
        desc: ''
      },
      cascaderOptions: [{
        value: 'guide',
        label: 'Guide',
        children: [{
          value: 'disciplines',
          label: 'Disciplines',
          children: [{
            value: 'consistency',
            label: 'Consistency'
          }, {
            value: 'feedback',
            label: 'Feedback'
          }]
        }, {
          value: 'navigation',
          label: 'Navigation',
          children: [{
            value: 'side nav',
            label: 'Side Navigation'
          }, {
            value: 'top nav',
            label: 'Top Navigation'
          }]
        }]
      }, {
        value: 'component',
        label: 'Component',
        children: [{
          value: 'basic',
          label: 'Basic',
          children: [{
            value: 'layout',
            label: 'Layout'
          }, {
            value: 'color',
            label: 'Color'
          }, {
            value: 'typography',
            label: 'Typography'
          }, {
            value: 'icon',
            label: 'Icon'
          }, {
            value: 'button',
            label: 'Button'
          }]
        }, {
          value: 'form',
          label: 'Form',
          children: [{
            value: 'radio',
            label: 'Radio'
          }, {
            value: 'checkbox',
            label: 'Checkbox'
          }, {
            value: 'input',
            label: 'Input'
          }, {
            value: 'input-number',
            label: 'InputNumber'
          }, {
            value: 'select',
            label: 'Select'
          }, {
            value: 'cascader',
            label: 'Cascader'
          }, {
            value: 'switch',
            label: 'Switch'
          }, {
            value: 'slider',
            label: 'Slider'
          }, {
            value: 'time-picker',
            label: 'TimePicker'
          }, {
            value: 'date-picker',
            label: 'DatePicker'
          }, {
            value: 'datetime-picker',
            label: 'DateTimePicker'
          }, {
            value: 'upload',
            label: 'Upload'
          }, {
            value: 'rate',
            label: 'Rate'
          }, {
            value: 'form',
            label: 'Form'
          }]
        }, {
          value: 'data',
          label: 'Data',
          children: [{
            value: 'table',
            label: 'Table'
          }, {
            value: 'tag',
            label: 'Tag'
          }, {
            value: 'progress',
            label: 'Progress'
          }, {
            value: 'tree',
            label: 'Tree'
          }, {
            value: 'pagination',
            label: 'Pagination'
          }, {
            value: 'badge',
            label: 'Badge'
          }]
        }, {
          value: 'notice',
          label: 'Notice',
          children: [{
            value: 'alert',
            label: 'Alert'
          }, {
            value: 'loading',
            label: 'Loading'
          }, {
            value: 'message',
            label: 'Message'
          }, {
            value: 'message-box',
            label: 'MessageBox'
          }, {
            value: 'notification',
            label: 'Notification'
          }]
        }, {
          value: 'navigation',
          label: 'Navigation',
          children: [{
            value: 'menu',
            label: 'NavMenu'
          }, {
            value: 'tabs',
            label: 'Tabs'
          }, {
            value: 'breadcrumb',
            label: 'Breadcrumb'
          }, {
            value: 'dropdown',
            label: 'Dropdown'
          }, {
            value: 'steps',
            label: 'Steps'
          }]
        }, {
          value: 'others',
          label: 'Others',
          children: [{
            value: 'dialog',
            label: 'Dialog'
          }, {
            value: 'tooltip',
            label: 'Tooltip'
          }, {
            value: 'popover',
            label: 'Popover'
          }, {
            value: 'card',
            label: 'Card'
          }, {
            value: 'carousel',
            label: 'Carousel'
          }, {
            value: 'collapse',
            label: 'Collapse'
          }]
        }]
      }, {
        value: 'resource',
        label: 'Resource',
        children: [{
          value: 'axure',
          label: 'Axure Components'
        }, {
          value: 'sketch',
          label: 'Sketch Templates'
        }, {
          value: 'docs',
          label: 'Design Documentation'
        }]
      }],
      radioGroup: 'Shanghai',
      dialogVisible: false,
      tableData: [{
        date: '2084-05-02',
        name: 'T-800',
        address: 'Los Angeles, Future Tech Blvd 101',
        status: 'ACTIVE',
        tagType: 'success'
      }, {
        date: '2084-05-04',
        name: 'HAL-9000',
        address: 'Jupiter Orbit, Discovery One',
        status: 'ERROR',
        tagType: 'danger'
      }, {
        date: '2084-05-01',
        name: 'R2-D2',
        address: 'Tatooine, Repair Shop',
        status: 'WAITING',
        tagType: 'warning'
      }, {
        date: '2084-05-03',
        name: 'WALL-E',
        address: 'Earth, Waste Allocation Sector',
        status: 'OFFLINE',
        tagType: 'info'
      }],
      dynamicTags: ['Tag 1', 'Tag 2', 'Tag 3'],
      inputVisible: false,
      inputValue: '',
      currentPage: 1,
      loading: true,
      fullscreenLoading: false,
      ruleForm: {
        name: '',
        region: ''
      },
      rules: {
        name: [
          { required: true, message: 'Please input Activity name', trigger: 'blur' },
          { min: 3, max: 5, message: 'Length should be 3 to 5', trigger: 'blur' }
        ],
        region: [
          { required: true, message: 'Please select Activity zone', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          alert('submit!');
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    openFullScreen1() {
      this.fullscreenLoading = true;
      setTimeout(() => {
        this.fullscreenLoading = false;
      }, 3000);
    },
    openFullScreen2() {
      const loading = this.$loading({
        lock: true,
        text: 'SYSTEM INITIALIZATION // 系统初始化',
        spinner: 'el-icon-loading',
        background: 'rgba(24, 24, 24, 0.95)',
        customClass: 'theme-loading-dark'
      });
      setTimeout(() => {
        loading.close();
      }, 3000);
    },
    handleSizeChange(val) {
      console.log(`${val} items per page`);
    },
    handleCurrentChange(val) {
      console.log(`current page: ${val}`);
    },
    handleClose(tag) {
      this.dynamicTags.splice(this.dynamicTags.indexOf(tag), 1);
    },

    showInput() {
      this.inputVisible = true;
      this.$nextTick(_ => {
        this.$refs.saveTagInput.$refs.input.focus();
      });
    },

    handleInputConfirm() {
      let inputValue = this.inputValue;
      if (inputValue) {
        this.dynamicTags.push(inputValue);
      }
      this.inputVisible = false;
      this.inputValue = '';
    },
    openMsgSuccess() {
      this.$message({
        message: 'Operation completed successfully.',
        type: 'success'
      })
    },
    openMsgWarning() {
      this.$message({
        message: 'Warning: System resource low.',
        type: 'warning'
      })
    },
    openDialog() {
      this.dialogVisible = true
    },
    openNotificationSuccess() {
      this.$notify({
        title: 'SUCCESS',
        message: 'The operation was completed successfully.',
        type: 'success',
        duration: 0 // Keep open for inspection
      });
    },
    openNotificationWarning() {
      this.$notify({
        title: 'WARNING',
        message: 'System resources are running low.',
        type: 'warning'
      });
    },
    openNotificationError() {
      this.$notify.error({
        title: 'ERROR',
        message: 'Critical system failure. Immediate action required.'
      });
    },
    openNotificationInfo() {
      this.$notify.info({
        title: 'INFO',
        message: 'New system update available.'
      });
    }
  }
}
</script>

<style scoped>
.theme-sample-page {
  padding: 40px;
  background-color: var(--color-bg);
  min-height: 100vh;
}

.sample-header {
  margin-bottom: 40px;
  border-bottom: 3px solid var(--color-black);
  padding-bottom: 20px;
}

.title {
  font-size: 48px;
  font-weight: 900;
  color: var(--color-black);
  text-transform: uppercase;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 20px;
  font-weight: bold;
  color: var(--brand-primary);
  letter-spacing: 2px;
}

.box-card {
  margin-bottom: 30px;
}

.section-label {
  font-weight: bold;
  color: #666;
  margin-bottom: 12px;
  margin-top: 0;
  font-size: 14px;
}

.color-palette {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.color-box {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 12px;
  font-weight: bold;
  border: 2px solid var(--color-black);
  box-shadow: 4px 4px 0 var(--color-black);
}

.typography-demo h1 { font-size: 32px; font-weight: 900; margin: 10px 0; }
.typography-demo h2 { font-size: 24px; font-weight: bold; margin: 10px 0; color: var(--brand-primary); }
.typography-demo p { font-size: 16px; line-height: 1.6; }

.demo-block {
  margin-bottom: 20px;
}

.el-tag + .el-tag {
  margin-left: 10px;
}

.button-new-tag {
  margin-left: 10px;
  height: 32px;
  line-height: 28px;
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}
</style>