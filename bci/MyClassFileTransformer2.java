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
 * 字节码转换器
 * 
 * 
 */
public class MyClassFileTransformer2 implements ClassFileTransformer {

	/**
	 * 字节码加载到虚拟机前会进入这个方法
	 */
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		// System.out.println(className);
		// 如果加载Numm类才拦截
		if (!"bci/Numm".equals(className)) {
			return null;
		}

		// javassist的包名是用点分割的，需要转换下
		if (className != null && className.indexOf("/") != -1) {
			className = className.replaceAll("/", ".");
			log("transform:Get it:" + className);
		}
		try {
			// 通过包名获取类文件
			ClassPool pool = ClassPool.getDefault();
			ClassClassPath classPath = new ClassClassPath(this.getClass());
			pool.insertClassPath(classPath);
			CtClass ccNumm = pool.get(className);

//			// 获得指定方法名的方法
			CtMethod setNum = ccNumm.getDeclaredMethod("setNum");
			// setNum.insertBefore("{ log(\"Before:\"+num); }");
			int linenum = setNum.insertAt(20, "{ System.out.println(\"Numm:trans insertAt:\"+$1); }");
			log("transform:linenum:" + linenum);

//
//			// 在方法执行前插入代码
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
	 * 在main函数执行前，执行的函数
	 * 
	 * @param options
	 * @param ins
	 */
	public static void premain(String options, Instrumentation ins) {
		// 注册我自己的字节码转换器
//		ins.addTransformer(new MyClassFileTransformer2());
	}

	private void log(String str) {
		System.out.println(str);
	}
}
