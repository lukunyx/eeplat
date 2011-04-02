package com.exedosoft.plat.action.customize.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.exedosoft.plat.ExedoException;
import com.exedosoft.plat.Transaction;
import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOBO;
import com.exedosoft.plat.ui.DOFormModel;
import com.exedosoft.plat.ui.DOFormTarget;

public class CopyFormsToGrid extends DOAbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3155689928718730452L;

	@Override
	public String excute() throws ExedoException {

		if (this.service==null || this.service.getTempSql() == null) {
			System.out.println("未配置SQL 语句");
			this.setEchoValue("未配置SQL 语句");
			return NO_FORWARD;
		}


		String gridModelUid = this.actionForm.getValue("gridModelUid");
		if (gridModelUid == null) {
			this.setEchoValue("没有选择表格！");
			return NO_FORWARD;
		}

		String[] checks = this.actionForm.getValueArray("checkinstance");
		if (checks == null) {
			this.setEchoValue("没有数据！");
			return NO_FORWARD;
		}
		Transaction t = this.service.currentTransaction();
		try {
			t.begin();
			DOBO boForm = DOBO.getDOBOByName("do_ui_formmodel");
			DOBO boFormTarget = DOBO.getDOBOByName("DO_UI_FormTargets");
			DOBO boFormLink = DOBO.getDOBOByName("DO_UI_FormLinks");
			for(int i = 0; i < checks.length ; i++){
				BOInstance biForm = boForm.getInstance(checks[i]);
				DOFormModel aFm = DOFormModel.getFormModelByID(biForm.getUid());
				
				biForm.putValue("objuid", null);
				biForm.putValue("gridModelUid", gridModelUid);
				BOInstance newBiForm = this.service.invokeUpdate(biForm);
				// //保存FormModel
				for(Iterator<DOFormTarget> itTargetGrid = aFm.getTargetGridModels().iterator();itTargetGrid.hasNext(); ){
					DOFormTarget aFt = itTargetGrid.next();
					BOInstance biFt = boFormTarget.getInstance(aFt.getObjUid());
					biFt.putValue("objuid", null);
					biFt.putValue("formUid", newBiForm.getUid());
					boFormTarget.getDInsertService().invokeUpdate(biFt);						
				}

					// //FormModel linkForms
				for(Iterator<DOFormModel> itLinkForms = aFm.getLinkForms().iterator(); itLinkForms.hasNext();){
						DOFormModel linkForm = itLinkForms.next();
						Map<String,String> paras = new HashMap<String,String>();
						paras.put("formuid", newBiForm.getUid());
						paras.put("linkformuid", linkForm.getObjUid());
						boFormLink.getDInsertService().invokeUpdate(paras);
				}
				
				
			}
			t.end();
		} catch (Exception e) {
			t.rollback();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return DEFAULT_FORWARD;

	}

}