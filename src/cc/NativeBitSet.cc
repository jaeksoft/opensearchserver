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
(JNIEnv *env, jobject obj, jlong size) {
  std::cout << "Init " << size << " - " << obj << "\n" << std::flush;
  boost::dynamic_bitset<> x(size);
  std::cout << "dynamic_bitset "<< &x << "\n" << std::flush;
  return (jlong)&x;
}



JNIEXPORT void JNICALL Java_com_jaeksoft_searchlib_util_bitset_NativeBitSet_set__JJ
(JNIEnv *env, jobject obj, jlong ref, jlong bit) {
  std::cout << "Set " << ref << " - " << bit << "\n" << std::flush;
}
