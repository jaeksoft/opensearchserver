CXX = g++
CPPFLAGS = -Wall -g -fPIC
INCLUDES = \
	-I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers \
	-I/Users/ekeller/C_Stuffs/boost_1_57_0
SRCS = NativeBitSet.cc
OBJS = $(SRCS:.cc=.o)
MAIN = nativeoss

.PHONY: depend clean

all: $(MAIN)

$(MAIN): $(OBJS)
	$(CXX) -lc -shared -fPIC -o $(MAIN) $(OBJS)

.cc.o:
	$(CXX) $(CPPFLAGS) $(INCLUDES) -c $< -o $@

clean:
	$(RM) *.o *~ $(MAIN)

depend: $(SRCS)
	makedepend $(INCLUDES) $^

# DO NOT DELETE THIS LINE -- make depend needs it

NativeBitSet.o: NativeBitSet.h
NativeBitSet.o: /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/jni.h
NativeBitSet.o: /usr/include/stdio.h /usr/include/sys/cdefs.h
NativeBitSet.o: /usr/include/sys/_symbol_aliasing.h
NativeBitSet.o: /usr/include/sys/_posix_availability.h
NativeBitSet.o: /usr/include/Availability.h
NativeBitSet.o: /usr/include/AvailabilityInternal.h /usr/include/_types.h
NativeBitSet.o: /usr/include/sys/_types.h /usr/include/machine/_types.h
NativeBitSet.o: /usr/include/i386/_types.h
NativeBitSet.o: /usr/include/sys/_pthread/_pthread_types.h
NativeBitSet.o: /usr/include/sys/_types/_va_list.h
NativeBitSet.o: /usr/include/sys/_types/_size_t.h
NativeBitSet.o: /usr/include/sys/_types/_null.h /usr/include/sys/stdio.h
NativeBitSet.o: /usr/include/sys/_types/_off_t.h
NativeBitSet.o: /usr/include/sys/_types/_ssize_t.h
NativeBitSet.o: /usr/include/secure/_stdio.h /usr/include/secure/_common.h
NativeBitSet.o: /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers/jni_md.h
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/lambda.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/core.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/transform_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/add_pointer.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/remove_reference.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/config.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/config/select_compiler_config.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/config/select_platform_config.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/config/suffix.hpp
NativeBitSet.o: /usr/include/limits.h /usr/include/machine/limits.h
NativeBitSet.o: /usr/include/i386/limits.h /usr/include/i386/_limits.h
NativeBitSet.o: /usr/include/sys/syslimits.h
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/detail/workaround.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/type_trait_def.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/template_arity_spec.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/int.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/int_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/adl_barrier.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/adl.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/msvc.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/intel.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/gcc.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/workaround.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/nttp_decl.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/nttp.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/cat.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/config/config.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/integral_wrapper.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/integral_c_tag.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/static_constant.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/static_cast.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/template_arity_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/preprocessor/params.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/preprocessor.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/comma_if.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/punctuation/comma_if.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/control/if.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/control/iif.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/logical/bool.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/facilities/empty.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/punctuation/comma.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repeat.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repetition/repeat.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/debug/error.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/detail/auto_rec.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/tuple/eat.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/inc.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/arithmetic/inc.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/lambda.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/ttp.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/ctps.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/overload_resolution.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/lambda_support.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/yes_no.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/arrays.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/na_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/preprocessor/enum.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/tuple/to_list.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/facilities/overload.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/variadic/size.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/tuple/size.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/list/for_each_i.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/list/adt.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/detail/is_binary.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/detail/check.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/logical/compl.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repetition/for.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repetition/detail/for.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/control/expr_iif.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/tuple/elem.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/facilities/expand.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/tuple/rem.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/tuple/detail/is_single_return.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/variadic/elem.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/type_trait_undef.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/add_reference.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_reference.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/config.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_lvalue_reference.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/bool_trait_def.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/integral_constant.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/bool.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/bool_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/integral_c.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/integral_c_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/bool_trait_undef.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_rvalue_reference.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/ice.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/yes_no_type.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/ice_or.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/ice_and.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/ice_not.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/ice_eq.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/remove_bounds.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/remove_pointer.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/cv_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/add_const.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/add_volatile.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/add_cv.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_const.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/cv_traits_impl.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_volatile.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/remove_const.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/remove_volatile.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/remove_cv.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/tuple/tuple.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/static_assert.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/ref.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/core/ref.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/utility/addressof.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/core/addressof.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/tuple/detail/tuple_basic.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/function_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_function.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/false_result.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/is_function_ptr_helper.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/utility/swap.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/core/swap.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/lambda_config.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/lambda_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/arity_code.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/actions.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/lambda_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/object_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/has_trivial_assign.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/intrinsics.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_pod.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_void.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_scalar.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_arithmetic.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_integral.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_float.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_enum.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_convertible.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_array.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_abstract.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_class.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_union.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/add_lvalue_reference.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/add_rvalue_reference.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/utility/declval.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_pointer.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_member_pointer.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/detail/is_mem_fun_pointer_tester.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/has_trivial_constructor.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/has_trivial_copy.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/has_trivial_destructor.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/has_nothrow_constructor.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/has_nothrow_copy.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/has_nothrow_assign.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_base_and_derived.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_same.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_compound.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_fundamental.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_empty.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_object.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_stateless.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/function_adaptors.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/has_xxx.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/na_spec.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/lambda_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/void_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/na.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/arity.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/dtp.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/preprocessor/def_params_tail.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/limits/arity.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/logical/and.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/logical/bitand.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/identity.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/facilities/identity.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/empty.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/arithmetic/add.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/arithmetic/dec.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/control/while.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/list/fold_left.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/list/detail/fold_left.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/list/fold_right.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/list/detail/fold_right.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/list/reverse.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/control/detail/while.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/arithmetic/sub.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/lambda_arity_param.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/eti.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/type_wrapper.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/has_xxx.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/msvc_typename.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/array/elem.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/array/data.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/array/size.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repetition/enum_params.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repetition/enum_trailing_params.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/same_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/utility/result_of.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/iteration/iterate.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/slot/slot.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/slot/detail/def.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repetition/enum_binary_params.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repetition/enum_shifted_params.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/facilities/intercept.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/if.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/value_wknd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/integral.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/eval_if.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/identity.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/or.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/use_preprocessed.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/nested_type_wknd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/include_preprocessed.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/mpl/aux_/config/compiler.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/stringize.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/is_member_function_pointer.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/utility/enable_if.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/core/enable_if.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/return_type_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/select_functions.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/lambda_functor_base.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/lambda_functors.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/is_placeholder.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/ret.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/operator_actions.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/operator_lambda_func_base.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/operator_return_type_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/is_instance_of.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/type_traits/conversion_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/enum_shifted_params.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/preprocessor/repeat_2nd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/indirect_reference.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/detail/is_incrementable.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/iterator/iterator_traits.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/detail/iterator.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/pointee.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/detail/container_fwd.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/operators.hpp
NativeBitSet.o: /Users/ekeller/C_Stuffs/boost_1_57_0/boost/lambda/detail/member_ptr.hpp
