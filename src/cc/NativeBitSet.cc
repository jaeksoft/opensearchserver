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

#include "NativeBitSet.h"
#include <boost/dynamic_bitset.hpp>
#include <iostream>


JNIEXPORT jlong JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_init
(JNIEnv *, jobject, jlong size) {
  boost::dynamic_bitset<> *bitSetRef  = new boost::dynamic_bitset<>(size);
  return (jlong) bitSetRef;
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_free
(JNIEnv *, jobject, jlong ref) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  if (bitSetRef != 0)
    delete bitSetRef;
}

JNIEXPORT jlong JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_size
(JNIEnv *, jobject, jlong ref) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  return bitSetRef == 0 ? 0 : bitSetRef->size();
}

JNIEXPORT jboolean JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_get
(JNIEnv *, jobject, jlong ref, jlong pos) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  return bitSetRef == 0 ? false : bitSetRef->test(pos);
}

JNIEXPORT jlong JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_clone
(JNIEnv *, jobject, jlong ref) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  if (bitSetRef == 0)
    return 0;
  bitSetRef = new boost::dynamic_bitset<>(*bitSetRef);
  return (jlong) bitSetRef;
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_set__J_3I
(JNIEnv *env, jobject, jlong ref, jintArray integers) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  if (bitSetRef == 0)
    return;
  jsize len = env->GetArrayLength(integers);
  jint *body = (jint*) env->GetPrimitiveArrayCritical(integers, 0);
  for (int i = 0; i < len; i++) {
    body++;
    bitSetRef->set(*body);
  }
  env->ReleasePrimitiveArrayCritical(integers, body, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_set__J_3JI
(JNIEnv *env, jobject, jlong ref, jlongArray longs, jint length) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  if (bitSetRef == 0)
    return;
  jlong *body = (jlong*) env->GetPrimitiveArrayCritical(longs, 0);
  for (int i = 0; i < length; i++)
    bitSetRef->set(body[i]);
  env->ReleasePrimitiveArrayCritical(longs, body, JNI_ABORT);
}

JNIEXPORT jlong JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_cardinality
(JNIEnv *, jobject, jlong ref) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  return bitSetRef == 0 ? 0 : bitSetRef->count();
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_flip
(JNIEnv *, jobject, jlong ref, jlong start, jlong end) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  if (bitSetRef == 0)
    return;
  if (end > bitSetRef->size())
    bitSetRef->resize(end);
  for (long i = start; i < end; i++)
    bitSetRef->flip(i);
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_and
(JNIEnv *, jobject, jlong ref, jlong ref2) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  boost::dynamic_bitset<> *bitSetRef2 = (boost::dynamic_bitset<>*)ref2;
  if (bitSetRef == 0 || bitSetRef2 == 0)
    return;
  *bitSetRef &= *bitSetRef2;
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_or
(JNIEnv *, jobject, jlong ref, jlong ref2) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  boost::dynamic_bitset<> *bitSetRef2 = (boost::dynamic_bitset<>*)ref2;
  if (bitSetRef == 0 || bitSetRef2 == 0)
    return;
  *bitSetRef |= *bitSetRef2;
}

JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_clear
(JNIEnv *, jobject, jlong ref, jlong bit) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  if (bitSetRef == 0)
    return;
  bitSetRef->reset(bit);
}

JNIEXPORT jlong JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_nextSetBit
(JNIEnv *, jobject, jlong ref, jlong pos) {
  boost::dynamic_bitset<> *bitSetRef = (boost::dynamic_bitset<>*)ref;
  if (bitSetRef == 0)
    return -1;
  pos--;
  std::size_t newPos = pos == -1 ? bitSetRef->find_first() : bitSetRef->find_next(pos);
  return newPos == boost::dynamic_bitset<>::npos ? -1 : newPos;
}
