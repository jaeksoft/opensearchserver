/**
* License Agreement for OpenSearchServer
*
* Copyright (C) 2014 Emmanuel Keller / Jaeksoft
*
* http://www.open-search-server.com
*
* This file is part of OpenSearchServer.
*
* OpenSearchServer is free software: you can redistribute it and/or modify it
* under the terms of the GNU General Public License as published by the Free
* Software Foundation, either version 3 of the License, or (at your option) any
* later version.
*
* OpenSearchServer is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
* details.
*
* You should have received a copy of the GNU General Public License along with
* OpenSearchServer. If not, see <http://www.gnu.org/licenses/>.
**/

#include "NativeFloatBufferedArray.h"
#include <boost/fusion/container/list.hpp>
#include <boost/fusion/include/list.hpp>
#include <iostream>
#include <string.h>

class array_node {

  jfloat *buffer;
  int size;
  array_node *next;

public:

  array_node(array_node *parent, jfloat* floats, int length) {
    int len = sizeof(jfloat) * length;
    buffer = (jfloat*) malloc(len);
    memcpy(buffer, floats, len);
    size = length;
    next = 0;
    if (parent != 0)
      parent->next = this;
  }

  void populate(jfloat* floats) {
    int len = sizeof(jint) * size;
    memcpy(floats, buffer, len);
    floats += size;
    if (next != 0)
      next->populate(floats);
  }

  virtual ~array_node() {
    if (buffer != 0) {
      free(buffer);
      buffer = 0;
    }
    if (next != 0) {
      delete next;
      next = 0;
    }
  }

};


class buffered_array {

  array_node *first;
  array_node *current;

public:

  long total_size;

  buffered_array() {
    total_size = 0;
    first = 0;
    current = 0;
  }

  void add(jfloat* floats, int length) {
    current = new array_node(current, floats, length);
    if (first == 0)
      first = current;
    total_size += length;
  }

  void populate(jfloat* floats) {
    if (first != 0)
      first->populate(floats);
  }

  virtual ~buffered_array() {
    if (first != 0) {
      delete first;
      first = 0;
    }
  }

};


JNIEXPORT jlong JNICALL Java_com_jaeksoft_searchlib_util_array_NativeFloatBufferedArray_init
(JNIEnv *, jobject, jlong maxSize) {
  buffered_array *ba  = new buffered_array();
  return (jlong) ba;
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_array_NativeFloatBufferedArray_free
(JNIEnv *, jobject, jlong ref) {
  buffered_array *ba = (buffered_array*)ref;
  if (ba != 0)
    delete ba;
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_array_NativeFloatBufferedArray_add
(JNIEnv *env, jobject, jlong ref, jfloatArray float_array, jint length) {
  buffered_array *ba = (buffered_array*)ref;
  if (ba == 0)
    return;
  jfloat *jfloats = (jfloat*) env->GetPrimitiveArrayCritical(float_array, 0);
  ba->add(jfloats, length);
  env->ReleasePrimitiveArrayCritical(float_array, jfloats, JNI_ABORT);
}


JNIEXPORT jlong JNICALL Java_com_jaeksoft_searchlib_util_array_NativeFloatBufferedArray_getSize
(JNIEnv *, jobject, jlong ref) {
  buffered_array *ba = (buffered_array*)ref;
  return ba == 0 ? 0 : ba->total_size;
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_array_NativeFloatBufferedArray_populateFinalArray
(JNIEnv *env, jobject, jlong ref, jfloatArray float_array) {
  buffered_array *ba = (buffered_array*)ref;
  if (ba ==0)
    return;
  jfloat *jfloats = (jfloat*) env->GetPrimitiveArrayCritical(float_array, 0);
  ba->populate(jfloats);
  env->ReleasePrimitiveArrayCritical(float_array, jfloats, 0);
}
