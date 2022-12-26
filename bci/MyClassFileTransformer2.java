package bci;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

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
public class MyClassFileTransformer2 implements ClassFileTransformer {

	/**
	 * �ֽ�����ص������ǰ������������
	 */
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// System.out.println(className);
		// �������Numm�������
		if (!"bci/Numm".equals(className)) {
			return null;
		}

		// javassist�İ������õ�ָ�ģ���Ҫת����
		if (className != null && className.indexOf("/") != -1) {
			className = className.replaceAll("/", ".");
			log("transform:Get it:" + className);
		}
		try {
			// ͨ��������ȡ���ļ�
			ClassPool pool = ClassPool.getDefault();
			ClassClassPath classPath = new ClassClassPath(this.getClass());
			pool.insertClassPath(classPath);
			CtClass ccNumm = pool.get(className);

//			// ���ָ���������ķ���
			CtMethod setNum = ccNumm.getDeclaredMethod("setNum");
			// setNum.insertBefore("{ log(\"Before:\"+num); }");
			int linenum = setNum.insertAt(20, "{ System.out.println(\"Numm:trans insertAt:\"+$1); }");
			log("transform:linenum:" + linenum);

//
//			// �ڷ���ִ��ǰ�������
//			setNum.insertBefore("{ System.out.println(\"Numm:Before:\"+num); }");
//			setNum.insertAfter("{ System.out.println(\"Numm:After:\"+num); }");

			CtMethod minus = CtNewMethod.make("public int minus(int input) {return num -= input; }", ccNumm);
			ccNumm.addMethod(minus);

			CtMethod add = ccNumm.getDeclaredMethod("add");
			add.setBody("{return minus($1);}");

			log("transform:Done.");
			// Compile
			return ccNumm.toBytecode();

		} catch (NotFoundException e) {
			log("NotFoundException\n" + e.getStackTrace());
		} catch (CannotCompileException e) {
			log("CannotCompileException\n" + e.getStackTrace());
		} catch (IOException e) {
			log("IOException\n" + e.getStackTrace());
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
//		ins.addTransformer(new MyClassFileTransformer2());
	}

	private void log(String str) {
		System.out.println(str);
	}
}
