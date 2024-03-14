package net.ibizsys.central.cloud.core.dataentity.logic;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ibizsys.central.cloud.core.IServiceSystemRuntime;
import net.ibizsys.central.cloud.core.dataentity.logic.DELogicSession;
import net.ibizsys.central.cloud.core.sysutil.ISysCloudLogUtilRuntime;
import net.ibizsys.central.dataentity.logic.DELogicDebugModes;
import net.ibizsys.central.dataentity.logic.IDELogicSession;
import net.ibizsys.runtime.util.LogLevels;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

public class DELogicRuntime extends net.ibizsys.central.dataentity.logic.DELogicRuntime {

	private static final Log log = LogFactory.getLog(DELogicRuntime.class);
	public static final String CONSOLESENDER_DELOGICDEBUGGER = "DELogic调试信息";
	private ISysCloudLogUtilRuntime iSysCloudLogUtilRuntime = null;
	
	
	@Override
	protected void onInit() throws Exception {
		if(this.getDebugMode() != DELogicDebugModes.NONE) {
			if(this.getSystemRuntime() instanceof IServiceSystemRuntime) {
				if(((IServiceSystemRuntime)this.getSystemRuntime()).isEnableProdMode()) {
					String strLogCat = String.format("%1$s|%2$s(%3$s)", this.getDataEntityRuntime().getName(), this.getLogicName(), this.getPSDELogic().getCodeName());
					log.warn(String.format("处理逻辑[%1$s]调试模式在生产模式下禁用", strLogCat));
					this.setDebugMode(DELogicDebugModes.NONE);
				}
			}
		}
		
		super.onInit();
	}
	
	protected ISysCloudLogUtilRuntime getSysCloudLogUtilRuntime() {
		if(this.iSysCloudLogUtilRuntime == null) {
			this.iSysCloudLogUtilRuntime = this.getDELogicRuntimeContext().getSystemRuntime().getSysUtilRuntime(ISysCloudLogUtilRuntime.class, false);
		}
		return this.iSysCloudLogUtilRuntime;
	}
	
	 
	
	@Override
	protected IDELogicSession createDELogicSession() {
		return new DELogicSession(this.getDELogicRuntimeContext());
	}

	
	@Override
	protected void outputDebugInfo(IDELogicSession iDELogicSession, Throwable ex) {
		super.outputDebugInfo(iDELogicSession, ex);
		
		if(this.isOutputDebugInfo()) {
			
			//格式化Console信息
			String strLogCat = String.format("%1$s|%2$s(%3$s)", this.getDataEntityRuntime().getName(), this.getLogicName(), this.getPSDELogic().getCodeName());
			
			String strInfo = getDebugConsoleInfo2(iDELogicSession, ex);
			if(StringUtils.hasLength(strInfo)) {
				String[] infos = splitByLength(strInfo, 2048000);
				for(String strPart: infos) {
					if(ex == null) {
						this.getSysCloudLogUtilRuntime().sendConsoleMessage(null, CONSOLESENDER_DELOGICDEBUGGER, false, LogLevels.INFO, strLogCat, strPart);
					}
					else {
						this.getSysCloudLogUtilRuntime().sendConsoleMessage(null, CONSOLESENDER_DELOGICDEBUGGER, false, LogLevels.ERROR, strLogCat, strPart);
					}
				}
			}
		}
	}
	
	public static String[] splitByLength(String str, int length) {
	    int strLength = str.length();
	    int arrayLength = (int) Math.ceil((double) strLength / length);
	    String[] result = new String[arrayLength];
	    for (int i = 0; i < arrayLength; i++) {
	        int beginIndex = i * length;
	        int endIndex = Math.min(beginIndex + length, strLength);
	        result[i] = str.substring(beginIndex, endIndex);
	    }
	    return result;
	}
	protected String getDebugConsoleInfo2(IDELogicSession iDELogicSession, Throwable ex) {
		StringBuilder sb = new StringBuilder();
		ObjectMapper objectMapper = new ObjectMapper();
		sb.append(String.format("实体[%1$s]处理逻辑[%2$s]\r\n%3$s", this.getDataEntityRuntimeBase().getName(), this.getPSDELogic().getName(),
					iDELogicSession.getDebugArrayNode().toString()));
		return sb.toString();
	}
	protected String getDebugConsoleInfo(IDELogicSession iDELogicSession, Throwable ex) {
		if(iDELogicSession.getDebugArrayNode() != null) {
			StringBuilder sb = new StringBuilder();
			int nSize = iDELogicSession.getDebugArrayNode().size();
			int nBlank = 0;
			int nBlankLevel = 2;
			
			for(int i= 0;i<nSize;i++) {
				JsonNode jsonNode = iDELogicSession.getDebugArrayNode().get(i);
				if(!(jsonNode instanceof ObjectNode)) {
					continue;
				}
				
				ObjectNode objectNode = (ObjectNode)jsonNode;
				JsonNode type = objectNode.get("type");
				JsonNode time = objectNode.get("time");
				JsonNode name = objectNode.get("name");
				JsonNode info = objectNode.get("info");
				JsonNode data = objectNode.get("data");
				JsonNode codeName = objectNode.get("codeName");
				
				if(type == null) {
					continue;
				}
				sb.append("\r\n");
				for(int j=0;j<nBlank;j++) {
					sb.append(" ");
				}
				if(time!=null) {
					sb.append(time.asText());
					sb.append(" ");
				}
				String strType = type.asText();
				sb.append(String.format("[%1$s]", strType));
				if(name!=null) {
					sb.append(String.format(" %1$s",name.asText()));
					if(codeName!=null) {
						sb.append(String.format("(%1$s)", codeName.asText()));
					}
					if(info!=null) {
						sb.append(String.format("， %1$s", info.asText()));
					}
				}
				else {
					if(info!=null) {
						sb.append(String.format(" %1$s",info.asText()));
					}
				}
				
				
				if(data!=null) {
					sb.append("\r\n");
					for(int j=0;j<nBlank;j++) {
						sb.append(" ");
					}
					sb.append(" ");
					sb.append(data.toPrettyString());
				}
				
				if(IDELogicSession.DEBUGTYPE_ENTERNODE.equals(strType)) {
					nBlank += nBlankLevel;
				}
				else
					if(IDELogicSession.DEBUGTYPE_EXITNODE.equals(strType)) {
						nBlank -= nBlankLevel;
						if(nBlank<0) {
							nBlank = 0;
						}
					}
			}
			sb.append("\r\n");
			sb.append("\r\n");
			return sb.toString();
		}
		
		return null;
	}
}
