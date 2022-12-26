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
 * �ֽ���ת����
 * 
 * 
 */
public class MyClassFileTransformer implements ClassFileTransformer {

	/**
	 * �ֽ�����ص������ǰ������������
	 */
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// log("className:" + className);
		// �������Numm�������
		if (!"bci/SomeCode".equals(className)) {
			return null;
		}

		// javassist�İ������õ�ָ�ģ���Ҫת����
		if (className != null && className.indexOf("/") != -1) {
			className = className.replaceAll("/", ".");
			log("transform:Get it:" + className);
		}

		List<FormatLog> formatLogList = readFile("D:\\workspace\\java\\ObjectAtLine.txt");

		try {
			// ͨ��������ȡ���ļ�
			ClassPool pool = ClassPool.getDefault();
			ClassClassPath classPath = new ClassClassPath(this.getClass());
			pool.insertClassPath(classPath);
			CtClass ccSomeCode = pool.get(className);

//			// ���ָ���������ķ���
			CtMethod runme = ccSomeCode.getDeclaredMethod("runme");
			// setNum.insertBefore("{ log(\"Before:\"+num); }");
//			int linenum = setNum.insertAt(20, "{ System.out.println(\"Numm:trans insertAt:\"+$1); }");
//			log("transform:linenum:" + linenum);

			CtMethod logFunc = CtNewMethod.make("private void log(Object obj) {System.out.println(obj);}", ccSomeCode);
			ccSomeCode.addMethod(logFunc);

//			// �ڷ���ִ��ǰ�������
			runme.insertBefore("{ log(\"" + className + ":Start\"); }");
			runme.insertAfter("{ log(\"" + className + ":End\"); }");

			// �ڵ�8�п�ʼ�ĵط����룬�������ֻ��һ��{����ô����뵽��һ��
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
	 * ��main����ִ��ǰ��ִ�еĺ���
	 * 
	 * @param options
	 * @param ins
	 */
	public static void premain(String options, Instrumentation ins) {
		// ע�����Լ����ֽ���ת����
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
	 * ��ȡһ��txt�ļ�
	 */
	private List<FormatLog> readFile(String fullFilePath) {

		List<FormatLog> dateList = new ArrayList<FormatLog>();

		Path path = Paths.get(fullFilePath);
		Scanner scanner = null;
		try {
			scanner = new Scanner(path);

			// һ��һ�еض�ȡ
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
			System.out.println("���ˣ�������");

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
	 * ׷��һЩlog ��Է�������ָ���в���log������������������ļ���
	 */
	private boolean insertLogsByLine(List<FormatLog> formatLogList, CtMethod ctMethod) throws Exception {

		try {
			for (FormatLog formatLog : formatLogList) {
				// .insertAt(8, "{ log(\"runme:num Bef\"+num.getNum()); }");
				StringBuffer logCode = new StringBuffer();
				//��log����ObjName
				if(formatLog.fullSize) {
					logCode.append("{ System.out.println(");
					logCode.append("\"" + formatLog.getLog() + ":\"");
					logCode.append("+" + formatLog.getObjName() );
					logCode.append("); }");
				}else {
					//ֻ��log
					if (formatLog.getLog() != null && "" != formatLog.getLog() && " " != formatLog.getLog()) {
						logCode.append("{ System.out.println(\"" + formatLog.getLog() + "\"); }");
					}else if(formatLog.getObjName() != null && "" != formatLog.getObjName() && " " != formatLog.getObjName()) {
						//ֻ��objName
						logCode.append("{ System.out.println(" + formatLog.getObjName() + "); }");
					}else {
						//ֻ���к�
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
