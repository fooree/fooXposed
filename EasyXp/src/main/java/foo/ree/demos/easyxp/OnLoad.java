package foo.ree.demos.easyxp;

/**
 * 当类被加载时，调用{@link #executeHook(Class)}对类进行hook。
 */
interface OnLoad {
    void executeHook(Class<?> cls);
}