package nc.bs.framework.server;

import java.lang.reflect.Method;

import nc.bs.framework.aop.Around;
import nc.bs.framework.aop.Behavior;
import nc.bs.framework.aop.PatternType;
import nc.bs.framework.aop.Pointcut;
import nc.bs.framework.aop.ProceedingJoinpoint;
import nc.bs.framework.common.NoProtect;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.exception.FrameworkSecurityException;

public class SecurityAspect {
	/*    */   private ITokenProcessor tp;
	/*    */ 
	/*    */   public SecurityAspect(ITokenProcessor tp)
	/*    */   {
	/* 26 */     this.tp = tp;
	/*    */   }
	/*    */ 
	/*    */   @Pointcut
	/*    */   public boolean needProcess(Method m) {
		
	/* 31 */     return (m.getAnnotation(NoProtect.class) == null);
	/*    */   }
	/*    */ 
	/*    */   @Around(pointcut="needProcess", patternType=PatternType.method)
	/*    */   @Behavior(Behavior.Mode.PERFLOW)
	/*    */   public Object aroundMethod1(ProceedingJoinpoint pjp) throws Throwable {
	/* 37 */     if (NetStreamContext.getToken() == null) {
	/* 38 */       throw new FrameworkSecurityException("invalid secrity token(null)");
	/*    */     }
	/*    */ 
//	/* 41 */     if (this.tp.verifyToken(NetStreamContext.getToken()) == null) {
//	/* 42 */       throw new FrameworkSecurityException("invalid secrity token");
//	/*    */     }
	/*    */ 
	/* 45 */     return pjp.proceed();
	/*    */   }
	/*    */ 

}
