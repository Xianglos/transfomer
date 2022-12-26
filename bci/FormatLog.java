package bci;

public class FormatLog {

	/* �к� */
	private int linenum;

	/* һЩ��˵�� */
	private String log;

	/* ��Ŀ�� */
	private String objName;

	/* �Ƿ�3����Ŀȫ�У�Ӱ�����log��ʽ */
	public boolean fullSize = false;

	public FormatLog() {
	}

	public FormatLog(String formatStr) {
		try {
			linenum = Integer.valueOf(formatStr.split(",")[0]);
			log = formatStr.split(",")[1];
			objName = formatStr.split(",")[2];
		} catch (Exception e) {
			e.getStackTrace();
		}
		setIsFullSize();
	}

	public int getLinenum() {
		return linenum;
	}

	public void setLinenum(int linenum) {
		setIsFullSize();
		this.linenum = linenum;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		setIsFullSize();
		this.log = log;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public void setIsFullSize() {
		if (log != null && log != "" & log != " ") {
			if (objName != null && objName != "" & objName != " ") {
				fullSize = true;
			}
		}
	}

	@Override
	public String toString() {
		return "FormatLog [linenum=" + linenum + ", log=" + log + ", objName=" + objName + ", fullSize=" + fullSize
				+ "]";
	}

}
