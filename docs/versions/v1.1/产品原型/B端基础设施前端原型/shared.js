/* ============================================================
   摊点通 v1.2 · 共享交互逻辑 (shared.js) · 墨璃版
   供所有页面复用：抽屉 / 弹窗 / Toast / 权限树 / 登录域切换
   第四版新增：按钮涟漪 / 菜单入场动画 / 数字滚动 / 实时时钟 / Ctrl+K
   ============================================================ */

/* ========== Toast ========== */
function toast(type, msg) {
  let wrap = document.getElementById('toast-wrap');
  if (!wrap) {
    wrap = document.createElement('div');
    wrap.id = 'toast-wrap';
    wrap.className = 'toast-wrap';
    document.body.appendChild(wrap);
  }
  const el = document.createElement('div');
  el.className = 'toast ' + type;
  const ic = { success: '✓', warn: '!', danger: '×', info: 'i' }[type] || 'i';
  el.innerHTML = '<span class="ti">' + ic + '</span>' + msg;
  wrap.appendChild(el);
  setTimeout(() => {
    el.style.opacity = '0';
    el.style.transition = 'opacity .3s';
    setTimeout(() => el.remove(), 300);
  }, 2600);
}

/* ========== 抽屉模板 ========== */
const DRAWERS = {
  'tenant-add': drawerShell('新增租户', '', `
    <div class="drawer-sec"><div class="drawer-sec-title">基础信息 <span class="req">*</span></div>
    <div class="form-grid col-1">
      <div class="form-item"><label>租户名称 <span class="req">*</span></label><input class="inp" placeholder="请输入租户名称"><div class="desc">租户名称将作为商户主体名称展示</div></div>
      <div class="form-item"><label>联系人 <span class="req">*</span></label><input class="inp" placeholder="请输入联系人姓名"></div>
      <div class="form-item"><label>联系电话 <span class="req">*</span></label><input class="inp" placeholder="请输入 11 位手机号"></div>
      <div class="form-item"><label>所在地区</label><div class="flex gap-8"><select class="inp" style="flex:1"><option>浙江省</option></select><select class="inp" style="flex:1"><option>杭州市</option></select><select class="inp" style="flex:1"><option>西湖区</option></select></div></div>
      <div class="form-item"><label>详细地址</label><input class="inp" placeholder="请输入详细地址"></div>
      <div class="form-item"><label>备注</label><textarea placeholder="选填，记录租户经营类型等说明"></textarea></div>
      <div class="form-item"><label>初始状态</label><div class="radio-group"><label class="radio checked"><span class="radio-dot"></span>启用</label><label class="radio"><span class="radio-dot"></span>停用</label></div><div class="desc">新建租户默认启用，可后续停用</div></div>
    </div></div>
  `, () => { closeAllDrawer(); toast('success', '租户创建成功'); }),

  'tenant-edit': drawerShell('编辑租户', '', `
    <div class="drawer-sec"><div class="drawer-sec-title">基础信息</div>
    <div class="form-grid col-1">
      <div class="form-item"><label>租户名称 <span class="req">*</span></label><input class="inp" value="湖滨小吃铺"></div>
      <div class="form-item disabled"><label>租户编号</label><input class="inp" value="TNT20260312001" disabled></div>
      <div class="form-item"><label>联系人 <span class="req">*</span></label><input class="inp" value="张明"></div>
      <div class="form-item"><label>联系电话 <span class="req">*</span></label><input class="inp" value="138****2468"></div>
      <div class="form-item"><label>所在地区</label><div class="flex gap-8"><select class="inp" style="flex:1"><option>浙江省</option></select><select class="inp" style="flex:1"><option>杭州市</option></select><select class="inp" style="flex:1"><option>西湖区</option></select></div></div>
      <div class="form-item"><label>详细地址</label><input class="inp" value="湖滨路 28 号沿街铺面"></div>
      <div class="form-item"><label>备注</label><textarea>主营商圈小吃，单门店经营</textarea></div>
    </div></div>
  `, () => { closeAllDrawer(); toast('success', '租户信息已保存'); }),

  'account-add': drawerShell('新增平台账号', '', `
    <div class="info-banner"><span class="ic"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg></span><div>该账号为<b>平台账号</b>，仅可登录平台管理端，可分配平台角色。</div></div>
    <div class="drawer-sec"><div class="drawer-sec-title">账号信息 <span class="req">*</span></div>
    <div class="form-grid col-1">
      <div class="form-item"><label>账号名称 <span class="req">*</span></label><input class="inp" placeholder="请输入账号名称"></div>
      <div class="form-item"><label>手机号 <span class="req">*</span></label><input class="inp" placeholder="请输入 11 位手机号"></div>
      <div class="form-item"><label>登录账号 <span class="req">*</span></label><input class="inp" placeholder="登录系统所用账号"><div class="desc">保存后不可修改</div></div>
      <div class="form-item"><label>所属角色 <span class="req">*</span></label><select><option value="">请选择平台角色</option><option>平台管理员</option><option>平台运营</option><option>客服专员</option><option>系统管理员</option></select></div>
      <div class="form-item"><label>初始状态</label><div class="radio-group"><label class="radio checked"><span class="radio-dot"></span>启用</label><label class="radio"><span class="radio-dot"></span>停用</label></div></div>
      <div class="form-item"><label>备注</label><textarea placeholder="选填"></textarea></div>
      <div class="form-item"><div class="form-tip">系统将为新账号生成临时密码，并通过短信发送至绑定手机号。首次登录后需修改密码。</div></div>
    </div></div>
  `, () => { closeAllDrawer(); toast('success', '平台账号创建成功，临时密码已发送'); }),

  'account-edit': drawerShell('编辑平台账号', '', `
    <div class="drawer-sec"><div class="drawer-sec-title">账号信息</div>
    <div class="form-grid col-1">
      <div class="form-item"><label>账号名称 <span class="req">*</span></label><input class="inp" value="周建国"></div>
      <div class="form-item disabled"><label>登录账号</label><input class="inp" value="zhoujianguo" disabled></div>
      <div class="form-item"><label>手机号 <span class="req">*</span></label><input class="inp" value="138****2468"></div>
      <div class="form-item"><label>所属角色 <span class="req">*</span></label><select><option>平台运营</option><option>平台管理员</option><option>客服专员</option><option>系统管理员</option></select></div>
      <div class="form-item"><label>状态</label><div class="radio-group"><label class="radio checked"><span class="radio-dot"></span>启用</label><label class="radio"><span class="radio-dot"></span>停用</label></div></div>
      <div class="form-item"><label>备注</label><textarea>平台运营一组</textarea></div>
    </div></div>
  `, () => { closeAllDrawer(); toast('success', '账号信息已保存'); }),

  'm-store-edit': drawerShell('编辑门店信息', 'merc', `
    <div class="info-banner merc"><span class="ic"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg></span><div>门店信息变更将记录至操作日志。第一阶段仅支持单门店编辑。</div></div>
    <div class="drawer-sec"><div class="drawer-sec-title">门店信息 <span class="req">*</span></div>
    <div class="form-grid col-1">
      <div class="form-item"><label>门店名称 <span class="req">*</span></label><input class="inp" value="湖滨小吃铺（湖滨路店）"></div>
      <div class="form-item disabled"><label>门店编号</label><input class="inp" value="ST20260312001001" disabled></div>
      <div class="form-item"><label>联系电话 <span class="req">*</span></label><input class="inp" value="138****2468"></div>
      <div class="form-item"><label>联系人 <span class="req">*</span></label><input class="inp" value="张明"></div>
      <div class="form-item"><label>所在地区</label><div class="flex gap-8"><select class="inp" style="flex:1"><option>浙江省</option></select><select class="inp" style="flex:1"><option>杭州市</option></select><select class="inp" style="flex:1"><option>西湖区</option></select></div></div>
      <div class="form-item"><label>详细地址</label><input class="inp" value="湖滨路 28 号沿街铺面"></div>
      <div class="form-item"><label>营业时间</label><div class="flex gap-8 items-center"><input class="inp" value="07:00" style="flex:1"><span class="text-tertiary">至</span><input class="inp" value="21:30" style="flex:1"></div><div class="desc">占位字段，第一阶段仅记录展示</div></div>
      <div class="form-item"><label>营业状态</label><div class="radio-group"><label class="radio merc checked"><span class="radio-dot"></span>营业中</label><label class="radio merc"><span class="radio-dot"></span>休息中</label></div></div>
    </div></div>
  `, () => { closeAllDrawer(); toast('success', '门店信息已保存'); }),

  'm-staff-add': drawerShell('新增员工', 'merc', `
    <div class="info-banner merc"><span class="ic"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg></span><div>新增员工仅限本商户范围。手机号与账号请使用<b>虚构示例</b>，不要录入真实个人信息。</div></div>
    <div class="drawer-sec"><div class="drawer-sec-title">员工信息 <span class="req">*</span></div>
    <div class="form-grid col-1">
      <div class="form-item"><label>员工姓名 <span class="req">*</span></label><input class="inp" placeholder="请输入员工姓名"></div>
      <div class="form-item"><label>手机号 <span class="req">*</span></label><input class="inp" placeholder="请输入 11 位手机号"><div class="desc">示例：139****5521（脱敏展示）</div></div>
      <div class="form-item"><label>登录账号 <span class="req">*</span></label><input class="inp" placeholder="登录账号，保存后不可修改"><div class="desc">示例：lixiufen</div></div>
      <div class="form-item"><label>所属角色 <span class="req">*</span></label><select><option value="">请选择商户角色</option><option>商户管理员</option><option>门店店员</option><option>财务查看员</option></select><div class="desc">仅可选择本商户范围内的角色</div></div>
      <div class="form-item"><label>状态</label><div class="radio-group"><label class="radio merc checked"><span class="radio-dot"></span>启用</label><label class="radio merc"><span class="radio-dot"></span>停用</label></div></div>
      <div class="form-item"><label>备注</label><textarea placeholder="选填"></textarea></div>
      <div class="form-item"><div class="form-tip">系统将为新员工生成临时密码，并通过短信发送至绑定手机号。员工首次登录后需修改密码。</div></div>
    </div></div>
  `, () => { closeAllDrawer(); toast('success', '员工创建成功，临时密码已发送'); }),

  'm-staff-edit': drawerShell('编辑员工', 'merc', `
    <div class="drawer-sec"><div class="drawer-sec-title">员工信息</div>
    <div class="form-grid col-1">
      <div class="form-item"><label>员工姓名 <span class="req">*</span></label><input class="inp" value="李秀芬"></div>
      <div class="form-item disabled"><label>登录账号</label><input class="inp" value="lixiufen" disabled></div>
      <div class="form-item"><label>手机号 <span class="req">*</span></label><input class="inp" value="139****5521"></div>
      <div class="form-item"><label>所属角色 <span class="req">*</span></label><select><option>门店店员</option><option>商户管理员</option><option>财务查看员</option></select></div>
      <div class="form-item"><label>状态</label><div class="radio-group"><label class="radio merc checked"><span class="radio-dot"></span>启用</label><label class="radio merc"><span class="radio-dot"></span>停用</label></div></div>
      <div class="form-item"><label>备注</label><textarea>早班店员</textarea></div>
    </div></div>
  `, () => { closeAllDrawer(); toast('success', '员工信息已保存'); })
};

function drawerShell(title, domain, bodyHtml, onSave) {
  const isMerc = domain === 'merc';
  const primaryCls = isMerc ? 'btn-primary merc' : 'btn-primary';
  const headCls = isMerc ? 'merc' : '';
  return `<div class="drawer-head ${headCls}"><h3><span class="accent-bar"></span>${title}</h3><div class="close" onclick="closeAllDrawer()">×</div></div>
  <div class="drawer-body">${bodyHtml}</div>
  <div class="drawer-foot"><button class="btn btn-secondary ${isMerc?'merc':''}" onclick="closeAllDrawer()">取消</button><button class="btn ${primaryCls}" onclick="(${onSave})()">保存</button></div>`;
}

function openDrawer(key) {
  ensureDrawerSlot();
  const slot = document.getElementById('drawer-slot');
  slot.innerHTML = DRAWERS[key] || '<div class="drawer-body" style="padding:40px;text-align:center;color:var(--text-tertiary)">模板开发中</div>';
  slot.classList.add('open');
  document.getElementById('drawer-mask').classList.add('open');
  document.body.style.overflow = 'hidden';
}

function closeAllDrawer() {
  const mask = document.getElementById('drawer-mask');
  const slot = document.getElementById('drawer-slot');
  if (mask) mask.classList.remove('open');
  if (slot) { slot.classList.remove('open'); slot.innerHTML = ''; }
  document.body.style.overflow = '';
}

/* ========== 弹窗模板 ========== */
const MODALS = {
  'tenant-disable': modalShell('warn', '确认停用该租户？', '停用后该租户将无法发起新的业务操作，历史数据予以保留。', `
    <div class="warn-box"><b>影响范围</b>：该租户下所有员工账号将无法登录商户管理端；门店信息与历史操作日志保留可查；可随时重新启用。该操作将记录至操作日志。</div>`,
    [{ t: '取消', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '确认停用', cls: 'btn-primary', act: "closeAllModal();toast('success','租户已停用')" }]),

  'tenant-enable': modalShell('info', '确认启用该租户？', '启用后该租户可恢复正常经营管理操作。', `
    <div class="modal-grid"><div class="row"><div class="k">租户名称</div><div class="v">南门咖啡档</div></div><div class="row"><div class="k">停用时长</div><div class="v">7 天</div></div></div>`,
    [{ t: '取消', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '确认启用', cls: 'btn-primary', act: "closeAllModal();toast('success','租户已启用')" }]),

  'account-disable': modalShell('warn', '确认停用该账号？', '停用后该账号将无法登录后台。', '<div class="warn-box"><b>影响</b>：账号立即失去登录能力，已建立的会话将失效。可随时重新启用。该操作将记录至操作日志。</div>',
    [{ t: '取消', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '确认停用', cls: 'btn-primary', act: "closeAllModal();toast('success','账号已停用')" }]),

  'account-enable': modalShell('info', '确认启用该账号？', '启用后该账号可重新登录后台。', '',
    [{ t: '取消', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '确认启用', cls: 'btn-primary', act: "closeAllModal();toast('success','账号已启用')" }]),

  'reset-pwd': modalShell('warn', '确认重置该账号密码？', '系统将生成临时密码并通过短信发送至账号绑定手机号。', '<div class="warn-box"><b>安全提示</b>：重置后原密码立即失效，账号需使用临时密码登录后修改。该操作将被记录至操作日志。</div>',
    [{ t: '取消', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '确认重置', cls: 'btn-primary', act: "closeAllModal();toast('success','密码已重置，临时密码已发送')" }]),

  'role-disable': modalShell('warn', '确认停用该角色？', '停用后该角色关联的账号将失去对应权限。', '<div class="warn-box"><b>影响</b>：关联账号的该角色权限将被移除，可能导致部分功能不可用。请确认关联账号已调整。该操作将记录至操作日志。</div>',
    [{ t: '取消', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '确认停用', cls: 'btn-primary', act: "closeAllModal();toast('success','角色已停用')" }]),

  'role-enable': modalShell('info', '确认启用该角色？', '启用后该角色可重新分配给账号。', '',
    [{ t: '取消', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '确认启用', cls: 'btn-primary', act: "closeAllModal();toast('success','角色已启用')" }]),

  'perm-confirm': modalShell('info', '保存角色权限配置？', '权限变更将立即对关联的全部账号生效，请确认配置无误。', `
    <div class="modal-grid"><div class="row"><div class="k">角色名称</div><div class="v">门店店员</div></div><div class="row"><div class="k">关联账号</div><div class="v">4 名员工</div></div><div class="row"><div class="k">变更权限</div><div class="v">新增 1 项 · 移除 0 项</div></div></div>`,
    [{ t: '取消', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '确认保存', cls: 'btn-primary merc', act: "closeAllModal();toast('success','权限配置已保存并生效')" }]),

  'perm-detail': modalShell('info', '权限点详情', '', `
    <div class="modal-grid"><div class="row"><div class="k">权限名称</div><div class="v">查看租户列表</div></div><div class="row"><div class="k">权限编码</div><div class="v"><span class="mono">tenant:view</span></div></div><div class="row"><div class="k">所属模块</div><div class="v">租户管理</div></div><div class="row"><div class="k">权限类型</div><div class="v"><span class="ptype ptype-menu">菜单</span></div></div><div class="row"><div class="k">状态</div><div class="v"><span class="tag tag-success">启用</span></div></div><div class="row"><div class="k">说明</div><div class="v">进入租户管理列表页，仅查看不涉及敏感操作</div></div><div class="row"><div class="k">维护方</div><div class="v">平台系统维护，普通管理员不可修改</div></div></div>`,
    [{ t: '关闭', cls: 'btn-primary', act: 'closeAllModal()' }]),

  'log-detail': modalShell('info', '操作日志详情', '', `
    <div class="modal-grid"><div class="row"><div class="k">追踪号</div><div class="v"><span class="mono">TR20260718114210002</span></div></div><div class="row"><div class="k">操作人</div><div class="v">系统管理员</div></div><div class="row"><div class="k">操作类型</div><div class="v"><span class="tag tag-warning">停用</span></div></div><div class="row"><div class="k">操作对象</div><div class="v">租户「南门咖啡档」（TNT20260228002）</div></div><div class="row"><div class="k">所属租户</div><div class="v">南门咖啡档</div></div><div class="row"><div class="k">操作时间</div><div class="v">2026-07-18 11:42:10</div></div><div class="row"><div class="k">操作结果</div><div class="v"><span class="tag tag-success">成功</span></div></div><div class="row"><div class="k">来源 IP</div><div class="v"><span class="mono">10.32.*.*</span></div></div><div class="row"><div class="k">请求方法</div><div class="v"><span class="mono">POST /api/tenant/toggle</span></div></div><div class="row"><div class="k">备注</div><div class="v">租户主动申请暂停经营，按要求停用</div></div></div>`,
    [{ t: '关闭', cls: 'btn-secondary', act: 'closeAllModal()' }, { t: '导出详情', cls: 'btn-secondary', act: "closeAllModal();toast('info','详情已导出')" }])
};

function modalShell(icType, title, desc, bodyHtml, btns) {
  const icChar = { warn: '!', danger: '×', info: 'i', success: '✓' }[icType] || 'i';
  const btnHtml = btns.map(b => `<button class="btn ${b.cls}" onclick="${b.act}">${b.t}</button>`).join('');
  const leadHtml = desc ? `<p class="modal-lead">${desc}</p>` : '';
  return `<div class="modal-head"><div class="modal-ic ${icType}">${icChar}</div><h3>${title}</h3></div><div class="modal-body">${leadHtml}${bodyHtml}</div><div class="modal-foot">${btnHtml}</div>`;
}

function openModal(key) {
  ensureModalSlot();
  const slot = document.getElementById('modal-slot');
  slot.innerHTML = MODALS[key] || '<div class="modal-body" style="padding:40px;text-align:center;color:var(--text-tertiary)">模板开发中</div>';
  slot.classList.toggle('wide', key === 'log-detail' || key === 'perm-detail');
  document.getElementById('modal-mask').classList.add('open');
  document.body.style.overflow = 'hidden';
}

function closeAllModal() {
  const mask = document.getElementById('modal-mask');
  const slot = document.getElementById('modal-slot');
  if (mask) mask.classList.remove('open');
  if (slot) slot.innerHTML = '';
  document.body.style.overflow = '';
}

/* ========== 权限树交互 ========== */
function toggleCheck(el) {
  el.classList.toggle('checked');
  el.classList.remove('indeterminate');
}

function togglePermGroup(el) {
  el.closest('.perm-group').classList.toggle('open');
}

/* ========== 开关切换 ========== */
function toggleSwitch(el) {
  el.classList.toggle('on');
}

/* ========== 单选切换 ========== */
function selectRadio(el) {
  const group = el.parentElement;
  group.querySelectorAll('.radio').forEach(r => r.classList.remove('checked'));
  el.classList.add('checked');
}

/* ========== 确保抽屉/弹窗/Toast 容器存在 ========== */
function ensureDrawerSlot() {
  if (!document.getElementById('drawer-mask')) {
    const mask = document.createElement('div');
    mask.id = 'drawer-mask';
    mask.className = 'drawer-mask';
    mask.onclick = closeAllDrawer;
    const slot = document.createElement('div');
    slot.id = 'drawer-slot';
    slot.className = 'drawer';
    document.body.appendChild(mask);
    document.body.appendChild(slot);
  }
}

function ensureModalSlot() {
  if (!document.getElementById('modal-mask')) {
    const mask = document.createElement('div');
    mask.id = 'modal-mask';
    mask.className = 'modal-mask';
    mask.onclick = function (e) { if (e.target === mask) closeAllModal(); };
    const slot = document.createElement('div');
    slot.id = 'modal-slot';
    slot.className = 'modal';
    mask.appendChild(slot);
    document.body.appendChild(mask);
  }
}

/* ========== ESC 关闭 ========== */
document.addEventListener('keydown', function (e) {
  if (e.key === 'Escape') {
    closeAllModal();
    closeAllDrawer();
  }
});

/* ========== 第四版增强：按钮涟漪效果 ========== */
function attachRipple() {
  document.querySelectorAll('.btn:not(.btn-link):not(.btn-ripple-bound)').forEach(function (btn) {
    btn.classList.add('btn-ripple-bound');
    btn.addEventListener('click', function (e) {
      var rect = btn.getBoundingClientRect();
      var ripple = document.createElement('span');
      ripple.className = 'btn-ripple';
      var size = Math.max(rect.width, rect.height);
      ripple.style.width = ripple.style.height = size + 'px';
      ripple.style.left = (e.clientX - rect.left - size / 2) + 'px';
      ripple.style.top = (e.clientY - rect.top - size / 2) + 'px';
      btn.appendChild(ripple);
      setTimeout(function () { ripple.remove(); }, 600);
    });
  });
}

/* ========== 第四版增强：菜单项滑入动画 ========== */
function animateMenuItems() {
  var items = document.querySelectorAll('.sidebar-menu .menu-item');
  items.forEach(function (item, idx) {
    item.style.opacity = '0';
    item.style.transform = 'translateX(-8px)';
    setTimeout(function () {
      item.style.transition = 'opacity .3s ease, transform .3s ease';
      item.style.opacity = '';
      item.style.transform = '';
    }, 80 + idx * 40);
  });
}

/* ========== 第四版增强：统计卡数字滚动 ========== */
function animateStatValue(el, target, duration) {
  duration = duration || 800;
  var start = 0;
  var startTime = null;
  var isInt = Number.isInteger(target);
  function step(ts) {
    if (!startTime) startTime = ts;
    var progress = Math.min((ts - startTime) / duration, 1);
    var eased = 1 - Math.pow(1 - progress, 3);
    var val = start + (target - start) * eased;
    el.textContent = isInt ? Math.round(val) : val.toFixed(1);
    if (progress < 1) requestAnimationFrame(step);
    else el.textContent = isInt ? target : target.toFixed(1);
  }
  requestAnimationFrame(step);
}

function initStatAnimations() {
  var observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        var el = entry.target;
        var raw = el.dataset.count || el.textContent.trim();
        var num = parseFloat(raw.replace(/,/g, ''));
        if (!isNaN(num) && num > 0 && !el.dataset.animated) {
          el.dataset.animated = '1';
          animateStatValue(el, num, 900);
        }
        observer.unobserve(el);
      }
    });
  }, { threshold: 0.4 });
  document.querySelectorAll('.stat-val').forEach(function (el) { observer.observe(el); });
}

/* ========== 第四版增强：登录页 domain 切换主色 ========== */
function switchLoginBg(isMerc) {
  var wrap = document.querySelector('.login-wrap');
  if (wrap) wrap.classList.toggle('merc', isMerc);
  var domain = document.querySelector('.login-domain');
  if (domain) domain.classList.toggle('merc', isMerc);
}

/* ========== 第四版增强：表格行点击高亮 ========== */
function initTableRowSelection() {
  document.querySelectorAll('.table tbody tr').forEach(function (tr) {
    var checkbox = tr.querySelector('.checkbox');
    if (!checkbox) return;
    tr.addEventListener('click', function (e) {
      if (e.target.closest('.btn-link, .link-text, a, button')) return;
      checkbox.classList.toggle('checked');
      tr.style.transition = 'background .2s';
      if (checkbox.classList.contains('checked')) {
        tr.style.background = 'var(--plat-50)';
      } else {
        tr.style.background = '';
      }
    });
  });
}

/* ========== 第四版增强：键盘快捷键（Ctrl+K 搜索） ========== */
function initKeyboardHints() {
  document.addEventListener('keydown', function (e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault();
      var searchIcon = document.querySelector('.tb-icon[title="搜索"]');
      if (searchIcon) {
        searchIcon.click();
        toast('info', '全局搜索已激活，输入关键词查找');
      }
    }
  });
}

/* ========== 第四版增强：实时时间显示 ========== */
function initLiveClock() {
  var clockEls = document.querySelectorAll('[data-clock]');
  if (!clockEls.length) return;
  function update() {
    var now = new Date();
    var y = now.getFullYear();
    var m = String(now.getMonth() + 1).padStart(2, '0');
    var d = String(now.getDate()).padStart(2, '0');
    var h = String(now.getHours()).padStart(2, '0');
    var mi = String(now.getMinutes()).padStart(2, '0');
    var s = String(now.getSeconds()).padStart(2, '0');
    var txt = y + '-' + m + '-' + d + ' ' + h + ':' + mi + ':' + s;
    clockEls.forEach(function (el) { el.textContent = txt; });
  }
  update();
  setInterval(update, 1000);
}

/* ========== 第四版增强：页面初始化总入口 ========== */
document.addEventListener('DOMContentLoaded', function () {
  attachRipple();
  animateMenuItems();
  initStatAnimations();
  initTableRowSelection();
  initKeyboardHints();
  initLiveClock();
});

/* 兼容动态插入的元素（抽屉/弹窗内按钮） */
var _rippleObserver = new MutationObserver(function () { attachRipple(); });
_rippleObserver.observe(document.body, { childList: true, subtree: true });
