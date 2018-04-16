//
// Created by fooree on 2018/4/14.
//
#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <string.h>
#include "include/inlineHook.h"

#define TAG "DumpLua"
#define LOG_INFO(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

#define PACKAGE_NAME "com.njp.one" // 目标应用的包名
//#define PACKAGE_NAME "target.app.package.name" //目标应用的包名
#define TARGET_SO "/data/data/%s/lib/libcocos2dlua.so" // 目标应用的libcocos2dlua.so

int (*origin_luaL_loadbuffer)(void *lua_state, char *buff, size_t size, char *name) = NULL;

int my_luaL_loadbuffer(void *lua_state, char *buff, size_t size, char *name) {
    LOG_INFO("lua size: %d, name: %s", (uint32_t) size, name);  // 打印lua脚本的大小和名称

//    以下代码将lua脚本写入以大小为名称的文件中，这样是有问题的。
//    其实应该以name命名文件，但是以上输出的日志会显示奇奇怪怪的名称。
//    具体细节，作为练习，自己研究吧。
//    char filename[64] = {0};
//    sprintf(filename, "/data/data/%s/%d.lua", PACKAGE_NAME, (unsigned int) size);
//    FILE *fp = fopen(filename, "w");
//    if (fp) {
//        fwrite(buff, size, 1, fp);
//        fclose(fp);
//    }

    return origin_luaL_loadbuffer(lua_state, buff, size, name);
}


//JNIEXPORT jint JNICALL __unused JNI_OnLoad(JavaVM *vm, void* reserved){ // 使用这行代码，我的环境编译出错
JNIEXPORT jint JNICALL __unused JNI_OnLoad(JavaVM *vm) {    // 我的环境只能使用这行代码，如果编译JNI_OnLoad出错，请使用上一行代码
    LOG_INFO("JNI_OnLoad enter");

    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) == JNI_OK) {
        LOG_INFO("GetEnv OK");

        char so_name[128] = {0};
        sprintf(so_name, TARGET_SO, PACKAGE_NAME);
        void *handle = dlopen(so_name, RTLD_NOW);
        if (handle) {
            LOG_INFO("dlopen() return %08x", (uint32_t) handle);
            void *luaL_loadbuffer = dlsym(handle, "luaL_loadbuffer");
            if (luaL_loadbuffer) {
                LOG_INFO("luaL_loadbuffer function address:%08X", (uint32_t) luaL_loadbuffer);
                if (ELE7EN_OK == registerInlineHook((uint32_t) luaL_loadbuffer,
                                                    (uint32_t) my_luaL_loadbuffer,
                                                    (uint32_t **) &origin_luaL_loadbuffer)) {
                    LOG_INFO("registerInlineHook luaL_loadbuffer success");
                    if (ELE7EN_OK == inlineHook((uint32_t) luaL_loadbuffer)) {
                        LOG_INFO("inlineHook luaL_loadbuffer success");
                    } else {
                        LOG_INFO("inlineHook luaL_loadbuffer failure");
                    }
                } else {
                    LOG_INFO("registerInlineHook luaL_loadbuffer failure");
                }
            } else {
                LOG_INFO("dlsym() failure");
            }
        } else {
            LOG_INFO("dlopen() failure");
        }
    } else {
        LOG_INFO("GetEnv failure");
    }

    LOG_INFO("JNI_OnLoad leave");
    return JNI_VERSION_1_6;
}