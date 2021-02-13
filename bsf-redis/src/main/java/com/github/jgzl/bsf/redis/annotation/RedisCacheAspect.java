package com.github.jgzl.bsf.redis.annotation;

import com.github.jgzl.bsf.redis.RedisException;
import com.github.jgzl.bsf.redis.RedisProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Huang Zhaoping
 */
@Slf4j
@Aspect
public class RedisCacheAspect {


    private static final String REDIS_CACHE_KEY = "method-cache:";
    private static SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    private RedisProvider redisProvider;

    public RedisCacheAspect(RedisProvider redisProvider) {
        this.redisProvider = redisProvider;
    }

    @Around("@annotation(redisCache)")
    public Object proceedRedisCache(ProceedingJoinPoint point, RedisCache redisCache) throws Throwable {
        String key = redisCache.key();
        if ((key = key.trim()).length() == 0) {
            key = getDefaultKey(point);
        } else if (key.contains("#")) {
            key = getSpelKey(point, key);
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Type returnType = signature.getMethod().getGenericReturnType();
        try {
            return redisProvider.cache(key, redisCache.timeout(), () -> {
                try {
                    return point.proceed();
                } catch (Throwable e) {
                    throw new RedisException(e);
                }
            }, returnType);
        } catch (Throwable e) {
            if (e instanceof RedisException) {
                throw e.getCause() == null ? e : e.getCause();
            } else {
                log.warn("Redis缓存异常，直接请求处理", e);
                return point.proceed();
            }
        }

    }

    private String getDefaultKey(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?> clazz = signature.getDeclaringType();
        StringBuilder sb = new StringBuilder();
        sb.append(REDIS_CACHE_KEY).append(clazz.getSimpleName()).append(".").append(signature.getMethod().getName());
        Object[] args = point.getArgs();
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                sb.append("_");
                if (arg != null && arg.getClass().isArray()) {
                    sb.append(Arrays.toString((Object[]) arg));
                } else {
                    sb.append(arg);
                }
            }
        }
        return sb.toString();
    }

    private String getSpelKey(ProceedingJoinPoint point, String spel) {
        Expression expression = spelExpressionParser.parseExpression(spel);
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(getParameters(point));
        return String.valueOf(expression.getValue(context));
    }

    private Map<String, Object> getParameters(ProceedingJoinPoint point) {
        Object[] paramValues = point.getArgs();
        if (paramValues == null || paramValues.length == 0) {
            return Collections.emptyMap();
        }
        Signature signature = point.getStaticPart().getSignature();
        String[] paramNames;
        if (signature instanceof CodeSignature) {
            paramNames = ((CodeSignature) signature).getParameterNames();
        } else {
            paramNames = new String[paramValues.length];
            for (int i = 0; i < paramValues.length; i++) {
                paramNames[i] = "arg" + i;
            }
        }
        Map<String, Object> params = new LinkedHashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            params.put(paramNames[i], paramValues[i]);
        }
        return params;
    }

}
