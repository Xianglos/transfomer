package bci;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * 字节码转换器
 * 
 * 
 */
public class MyClassFileTransformer implements ClassFileTransformer {

	/**
	 * 字节码加载到虚拟机前会进入这个方法
	 */
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// log("className:" + className);
		// 如果加载Numm类才拦截
		if (!"bci/SomeCode".equals(className)) {
			return null;
		}

		// javassist的包名是用点分割的，需要转换下
		if (className != null && className.indexOf("/") != -1) {
			className = className.replaceAll("/", ".");
			log("transform:Get it:" + className);
		}

		List<FormatLog> formatLogList = readFile("D:\\workspace\\java\\ObjectAtLine.txt");

		try {
			// 通过包名获取类文件
			ClassPool pool = ClassPool.getDefault();
			ClassClassPath classPath = new ClassClassPath(this.getClass());
			pool.insertClassPath(classPath);
			CtClass ccSomeCode = pool.get(className);

//			// 获得指定方法名的方法
			CtMethod runme = ccSomeCode.getDeclaredMethod("runme");
			// setNum.insertBefore("{ log(\"Before:\"+num); }");
//			int linenum = setNum.insertAt(20, "{ System.out.println(\"Numm:trans insertAt:\"+$1); }");
//			log("transform:linenum:" + linenum);

			CtMethod logFunc = CtNewMethod.make("private void log(Object obj) {System.out.println(obj);}", ccSomeCode);
			ccSomeCode.addMethod(logFunc);

//			// 在方法执行前插入代码
			runme.insertBefore("{ log(\"" + className + ":Start\"); }");
			runme.insertAfter("{ log(\"" + className + ":End\"); }");

			// 在第8行开始的地方插入，如果这行只有一个{，那么会插入到下一行
//			runme.insertAt(8, "{ log(\"runme:num Bef\"+num.getNum()); }");
			insertLogsByLine(formatLogList, runme);

			log("transform:Done.");
			// Compile
			return ccSomeCode.toBytecode();

		} catch (NotFoundException e) {
			log("NotFoundException\n" + e.getStackTrace());
		} catch (CannotCompileException e) {
			log("CannotCompileException\n" + e.getStackTrace());
//		} catch (IOException e) {
//			log("IOException\n" + e.getStackTrace());
		} catch (Exception e) {
			log("Exception\n" + e.getStackTrace());
		}
		return null;
	}

	/**
	 * 在main函数执行前，执行的函数
	 * 
	 * @param options
	 * @param ins
	 */
	public static void premain(String options, Instrumentation ins) {
		// 注册我自己的字节码转换器
		ins.addTransformer(new MyClassFileTransformer());
	}

	/**
	 * print log to console
	 * 
	 * @param options
	 * @param ins
	 */
	private void log(String str) {
		System.out.println(str);
	}

	/**
	 * 读取一个txt文件
	 */
	private List<FormatLog> readFile(String fullFilePath) {

		List<FormatLog> dateList = new ArrayList<FormatLog>();

		Path path = Paths.get(fullFilePath);
		Scanner scanner = null;
		try {
			scanner = new Scanner(path);

			// 一行一行地读取
			while (scanner.hasNextLine()) {

				// process each line
				String line = scanner.nextLine();

				// this line isn't empty
				if (line != null && "" != line && " " != line) {
					// Comments will be ignored
					if (line.indexOf("#") == 0) {
						continue;
					} else {
						dateList.add(new FormatLog(line));
					}
				}

			}
			scanner.close();
		} catch (Exception e) {
			System.out.println("挂了，重启吧");

			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
				scanner = null;
			}
		}

		return dateList;

	}

	/**
	 * 追加一些log 针对方法，在指定行插入log，行数是相对于整个文件的
	 */
	private boolean insertLogsByLine(List<FormatLog> formatLogList, CtMethod ctMethod) throws Exception {

		try {
			for (FormatLog formatLog : formatLogList) {
				// .insertAt(8, "{ log(\"runme:num Bef\"+num.getNum()); }");
				StringBuffer logCode = new StringBuffer();
				//有log、有ObjName
				if(formatLog.fullSize) {
					logCode.append("{ System.out.println(");
					logCode.append("\"" + formatLog.getLog() + ":\"");
					logCode.append("+" + formatLog.getObjName() );
					logCode.append("); }");
				}else {
					//只有log
					if (formatLog.getLog() != null && "" != formatLog.getLog() && " " != formatLog.getLog()) {
						logCode.append("{ System.out.println(\"" + formatLog.getLog() + "\"); }");
					}else if(formatLog.getObjName() != null && "" != formatLog.getObjName() && " " != formatLog.getObjName()) {
						//只有objName
						logCode.append("{ System.out.println(" + formatLog.getObjName() + "); }");
					}else {
						//只有行号
						continue;
					}
					
				}
				
				ctMethod.insertAt(formatLog.getLinenum(), logCode.toString());
			}
		} catch (CannotCompileException e) {
			throw e;
		}

		return true;

	}
}
