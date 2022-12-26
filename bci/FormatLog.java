package bci;

public class FormatLog {

	/* 行号 */
	private int linenum;

	/* 一些想说的 */
	private String log;

	/* 项目名 */
	private String objName;

	/* 是否3个项目全有（影响输出log格式 */
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
