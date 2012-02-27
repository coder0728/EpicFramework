//package com.epic.framework.common.Ui2;
//
//import com.epic.framework.common.Ui2.JSON.JSONObject;
//import com.epic.framework.common.util.exceptions.EpicObjectInflationException;
//
//public class ClassEnsureLoginAction extends EpicClass {
//	public static final ClassEnsureLoginAction singleton = new ClassEnsureLoginAction();
//	
//	@Override
//	public EpicObject inflate(JSONObject data) {
//		EnsureLoginAction object = new EnsureLoginAction();
//		
//		object.failure = (EpicAction) Registry.inflateField(data, "failure", ClassEpicAction.singleton, EpicObject.FIELD_OPTIONAL);
//		object.success = (EpicAction) Registry.inflateField(data, "success", ClassEpicAction.singleton, EpicObject.FIELD_OPTIONAL);
//		
//		return object;
//	}
//	
//	public static void register() {
//		Registry.register("com.epic.framework.common.Ui2.EnsureLoginAction", singleton);
//	}
//
//}