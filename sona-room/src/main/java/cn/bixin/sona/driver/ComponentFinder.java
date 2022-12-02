package cn.bixin.sona.driver;

import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.bixin.sona.annotation.Constants;
import cn.bixin.sona.util.SonaLogger;

/**
 * component查找器
 *
 * @Author luokun
 * @Date 2020/3/25
 */
public class ComponentFinder {

    /**
     * 获取Component
     *
     * @param component
     * @param <T>
     * @return
     */
    @Nullable
    public static <T> T find(Class<T> component) {
        if (component != null) {
            try {
                Class cls = Class.forName(Constants.PACKAGE + "." + Constants.NAME + component.getSimpleName());
                Object obj = cls.newInstance();
                Method mtd = cls.getDeclaredMethod("create", new Class[]{});
                Object target = mtd.invoke(obj);
                return (T) target;
            } catch (ClassNotFoundException e) {
                SonaLogger.print("ComponentFinder ClassNotFoundException ${e.getMessage()}");
            } catch (NoSuchMethodException e) {
                SonaLogger.print("ComponentFinder NoSuchMethodException ${e.getMessage()}");
            } catch (IllegalAccessException e) {
                SonaLogger.print("ComponentFinder IllegalAccessException ${e.getMessage()}");
            } catch (InstantiationException e) {
                SonaLogger.print("ComponentFinder InstantiationException ${e.getMessage()}");
            } catch (InvocationTargetException e) {
                SonaLogger.print("ComponentFinder InvocationTargetException ${e.getMessage()}");
            }
        }
        return null;
    }
}
