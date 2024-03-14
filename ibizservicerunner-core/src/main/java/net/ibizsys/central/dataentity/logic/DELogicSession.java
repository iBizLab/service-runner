package net.ibizsys.central.dataentity.logic;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ibizsys.central.dataentity.logic.*;
import net.ibizsys.model.dataentity.logic.IPSDELogic;
import net.ibizsys.model.dataentity.logic.IPSDELogicLink;
import net.ibizsys.model.dataentity.logic.IPSDELogicNode;
import net.ibizsys.psmodel.core.util.IPSModel;
import net.ibizsys.psmodel.core.util.IPSModelServiceSession;
import net.ibizsys.psmodel.core.util.PSModelServiceSession;
import net.ibizsys.psmodel.runtime.util.IPSModelRTServiceSession;
import net.ibizsys.psmodel.runtime.util.PSModelRTServiceSession;
import net.ibizsys.psmodel.runtime.util.PSModelRTStorage;
import net.ibizsys.runtime.security.IUserContext;
import net.ibizsys.runtime.security.UserContext;
import net.ibizsys.runtime.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 实体逻辑会话接口实现
 * @author lionlau
 *
 */
public class DELogicSession implements IDELogicSession {

	private static final Log log = LogFactory.getLog(DELogicSession.class);

	private static ThreadLocal<IDELogicSession> current = new ThreadLocal<IDELogicSession>();

	private Map<String, Object> paramMap = new HashMap<>();

	private Map<String, Object> lastparamMap = new HashMap<>();
	private Object result = null;
	private Object lastReturn = null;
	private IDELogicRuntimeContext iDELogicRuntimeContext = null;
	//private IAppContext iAppContext = null;

	private ArrayNode debugArrayNode = null;

	public static IDELogicSession getCurrent() {
		return current.get();
	}

	public static void setCurrent(IDELogicSession iDELogicSession) {
		current.set(iDELogicSession);
	}

	/**
	 * 获取当前会话，必须存在
	 *
	 * @return
	 */
	static public IDELogicSession getCurrentMust() {
		IDELogicSession iDELogicSession = getCurrent();
		if(iDELogicSession==null) {
			throw new RuntimeException("当前逻辑会话无效");
		}
		return iDELogicSession;
	}


	public DELogicSession(IDELogicRuntimeContext iDELogicRuntimeContext) {
		this.iDELogicRuntimeContext = iDELogicRuntimeContext;
		this.debugArrayNode = JsonUtils.createArrayNode();
	}

	protected IDELogicRuntimeContext getDELogicRuntimeContext() {
		return this.iDELogicRuntimeContext;
	}

	@Override
	@Deprecated
	public IEntity getParam(String strName) throws Throwable {
		return this.getParam(strName, false);
	}

	@Override
	@Deprecated
	public IEntity getParam(String strName, boolean bTryMode) throws Throwable {
		Object obj =  paramMap.get(strName);
		IEntity iEntity = null;
		if(obj!=null) {
			if(obj instanceof IEntity) {
				iEntity = (IEntity)obj;
			}
			else {
				throw new Exception(String.format("参数[%1$s]类型不正确", strName));
			}
		}

		if(iEntity != null || bTryMode) {
			return iEntity;
		}
		throw new Exception(String.format("未存在指定参数[%1$s]", strName));
	}

	@Override
	@Deprecated
	public void setParam(String strName, IEntity iEntity) {
		paramMap.put(strName, iEntity);
	}




	@Override
	public Object getParamObject(String strName) throws Throwable {
		return this.getParamObject(strName, false);
	}

	@Override
	public void setParamObject(String strName, Object object) {
		paramMap.put(strName, object);
	}

	@Override
	public Object getParamObject(String strName, boolean bTryMode) throws Throwable {
		Object obj =  paramMap.get(strName);
		if(obj != null || bTryMode) {
			return obj;
		}
		throw new Exception(String.format("未存在指定参数[%1$s]", strName));
	}

	@Override
	public IUserContext getUserContext() {
		ActionSession actionSession = ActionSessionManager.getCurrentSession();
		if (actionSession != null && actionSession.getUserContext() != null) {
			return actionSession.getUserContext();
		}
		return UserContext.getCurrent();
	}


	@Override
	public Object getResult() {
		return this.result;
	}

	@Override
	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public Object getLastReturn() {
		return this.lastReturn;
	}

	@Override
	public void setLastReturn(Object lastReturn) {
		this.lastReturn = lastReturn;
	}

	@Override
	public void debugEnterNode(IDELogicNodeRuntime iDELogicNodeRuntime, IPSDELogicNode iPSDELogicNode) {

		if(log.isDebugEnabled()) {
			log.debug(String.format("进入节点[%1$s]", iPSDELogicNode.getName()));
		}

		if(getDebugArrayNode() == null) {
			return;
		}

		ObjectNode objectNode =	getDebugArrayNode().addObject();
		objectNode.put("type", "enternode");
		objectNode.put("time", DateUtils.getCurTimeString2());
		objectNode.put("name", iPSDELogicNode.getName());
		objectNode.put("codeName", iPSDELogicNode.getCodeName());
		if(this.getDELogicRuntime().getDebugMode()==DELogicDebugModes.INFO) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				if (iPSDELogicNode.getLogicNodeType().equals(DELogicNodeTypes.BEGIN)) {
					IPSModel domain = getLogicModel();
					String strLogicModel = objectMapper.writeValueAsString(domain);
					String strLogicid = String.format("%1$s.%2$s",this.getDELogicRuntime().getDataEntityRuntime().getName(),this.getDELogicRuntime().getPSDELogic().getCodeName());
					objectNode.put("logicmodel", strLogicModel);
					objectNode.put("logicid",strLogicid.toLowerCase());
				}
				Map<String, Object> nodeParamMap = new HashMap<>();;
				for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
					String key = entry.getKey();
					Object valueA = entry.getValue();
					Object valueB = lastparamMap.get(key);
					if(valueB != null && valueA == null){
						nodeParamMap.put(key, valueA);
					} else if (valueB == null || !valueA.equals(valueB)) {
						nodeParamMap.put(key, valueA);
					}
				}
				lastparamMap.putAll(paramMap);
				String strParamsData = objectMapper.writeValueAsString(nodeParamMap);
				objectNode.put("paramsdata", strParamsData);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	protected IPSModel getLogicModel() throws Exception{
		IPSModel domain = null;
		PSModelRTServiceSession psModelServiceSession = new PSModelRTServiceSession();
		IPSModelServiceSession lastPSModelServiceSession = null;
		try {
			lastPSModelServiceSession = PSModelServiceSession.getCurrent(true);
			PSModelRTStorage devOpsDynaModelAPIPSModelRTStorage = new PSModelRTStorage();
			psModelServiceSession.setPSSystemService(this.getDELogicRuntime().getSystemRuntime().getPSSystemService());
			psModelServiceSession.setPSModelStorage(devOpsDynaModelAPIPSModelRTStorage);
			PSModelServiceSession.setCurrent(psModelServiceSession);
			domain = getPSModelRTServiceSession().getPSModelListTranspiler(IPSDELogic.class, false).decompile(getPSModelRTServiceSession(), this.getDELogicRuntime().getPSModelObject(), domain, true);
			return domain;
		} finally {
			PSModelServiceSession.setCurrent(lastPSModelServiceSession);
		}
	}
	protected IPSModelRTServiceSession getPSModelRTServiceSession() throws Exception {
		return ((IPSModelRTServiceSession) PSModelServiceSession.getCurrent());
	}
	@Override
	public void debugExitNode(IDELogicNodeRuntime iDELogicNodeRuntime, IPSDELogicNode iPSDELogicNode) {

		if(log.isDebugEnabled()) {
			log.debug(String.format("离开节点[%1$s]", iPSDELogicNode.getName()));
		}

		if(getDebugArrayNode() == null) {
			return;
		}
		ObjectNode objectNode =	getDebugArrayNode().addObject();
		objectNode.put("type", "exitnode");
		objectNode.put("time", DateUtils.getCurTimeString2());
		objectNode.put("name", iPSDELogicNode.getName());
		objectNode.put("codeName", iPSDELogicNode.getCodeName());

	}



	@Override
	public void debugEnterLink(IDELogicNodeRuntime iDELogicNodeRuntime, IPSDELogicNode iPSDELogicNode, IPSDELogicLink iPSDELogicLink) {

		if(log.isDebugEnabled()) {
			log.debug(String.format("进入连接[%1$s@%2$s]", iPSDELogicLink.getName(), iPSDELogicNode.getName()));
		}


		if(getDebugArrayNode() == null) {
			return;
		}
		ObjectNode objectNode =	getDebugArrayNode().addObject();
		objectNode.put("type", "enterlink");
		objectNode.put("time", DateUtils.getCurTimeString2());
		objectNode.put("name", String.format("%1$s@%2$s", iPSDELogicLink.getName(), iPSDELogicNode.getName()));
	}



	@Override
	public void debugParam(IDELogicParamRuntime iDELogicParamRuntime) {

		if(getDebugArrayNode() == null) {
			return;
		}
		ObjectNode objectNode =	getDebugArrayNode().addObject();
		objectNode.put("type", "debugparam");
		objectNode.put("time", DateUtils.getCurTimeString2());
		iDELogicParamRuntime.debug(this, objectNode);

		if(log.isDebugEnabled()) {
			log.debug(String.format("输出参数[%1$s]\r\n%2$s", iDELogicParamRuntime.getName(), objectNode));
		}

	}

	@Override
	public void debugInfo(String strInfo) {
		if(log.isDebugEnabled()) {
			log.debug(strInfo);
		}

		if(getDebugArrayNode() == null) {
			return;
		}
		ObjectNode objectNode =	getDebugArrayNode().addObject();
		objectNode.put("type", "debuginfo");
		objectNode.put("time", DateUtils.getCurTimeString2());
		objectNode.put("info", strInfo);
	}


	public ArrayNode getDebugArrayNode() {
		return this.debugArrayNode;
	}

	@Override
	public IAppContext getAppContext() {

		if (ActionSessionManager.getCurrentSession() != null) {
			return ActionSessionManager.getCurrentSession().getAppContext();
		}
		else {
			return UserContext.getCurrentMust().getAppContext();
		}

//		if(this.iAppContext == null) {
//
//		}
//		return this.iAppContext;
	}

//	@Override
//	public void setAppContext(IAppContext iAppContext) {
//		this.iAppContext = iAppContext;
//	}


	@Override
	public IDELogicRuntime getDELogicRuntime() {
		return this.getDELogicRuntimeContext().getDELogicRuntime();
	}





//	@Override
//	public Object value(String strParam, String strField) throws Throwable {
//		IDELogicParamRuntime iDELogicParamRuntime = this.getDELogicRuntimeContext().getDELogicRuntime().getDELogicParamRuntime(strParam, false);
//		if(StringUtils.hasLength(strField)) {
//			return iDELogicParamRuntime.get(strField);
//		}
//		else {
//			return iDELogicParamRuntime.getReal();
//		}
//	}
//
//	@Override
//	public Object value(String strParam) throws Throwable {
//		return value(strParam, null);
//	}


}
