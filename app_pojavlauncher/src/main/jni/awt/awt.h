//
// Created by whbex on 20.08.2026.
//

#ifndef POJAVLAUNCHER_AWT_H
#define POJAVLAUNCHER_AWT_H

#include <jni.h>
#include <bits/pthread_types.h>
#include <stdbool.h>

#define EVENT_TYPE_CHAR 1000
#define EVENT_TYPE_CURSOR_POS 1003
#define EVENT_TYPE_KEY 1005
#define EVENT_TYPE_MOUSE_BUTTON 1006

#define CANVAS_WIDTH 1024
#define CANVAS_HEIGHT 768

extern JavaVM* androidVM;
extern JavaVM* runtimeVM;

// Runtime VM can appear later
extern pthread_mutex_t vm_wait_mutex;
extern pthread_cond_t vm_wait_cond;
extern _Atomic bool isVmConnected;

extern float inputXRatio;
extern float inputYRatio;


extern JNIEnv* JNIEnv_InputRuntime;

void register_methods_clipboard(JNIEnv* env);
void register_methods_util(JNIEnv* env);

jint translate_awt_mouse(jint android_mousekey);
jint translate_awt_keycode(jint android_keycode);

#endif //POJAVLAUNCHER_AWT_H
