#
#  -- Time Module
#  -- Version 1, October 2022
#  -- Various time related functions (local time)
#  -- Recuires libc
#  -- Marinos Pappas
# 
# x86-64 Assembly Code - AT&T format
# library time
# compiled on Wed Feb 19 19:30:27 CET 2025
.data
.align 8
# TINSEL version 4.0 for x86-84 (Linux) February 2025 (c) M.Pappas\n
	CLOCK_REALTIME:	.quad 0
	tv:	.space 16
.align 8
	tm:	.space 80
.align 8
	time_now:	.quad 0

.text
.align 8
.extern clock_gettime_tnsl
.extern ctime
.extern localtime_r

.global getlocaltime
# function getlocaltime
getlocaltime:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$8, %rsp
	movq	%rdi, -8(%rbp)
# parameter cur_time offset from frame -8
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tv(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime_tnsl
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	movq	%rax, time_now(%rip)
	lea	time_now(%rip), %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tm(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	localtime_r
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$8, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$1, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$4, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$2, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$3, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$12, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$4, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$16, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	pushq	%rax
	movq	$1, %rax
	testq	%rax, %rax
	popq	%rbx
	addq	%rbx, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	$5, %rax
	testq	%rax, %rax
	movq	%rax, %r10
	movq	$20, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tm(%rip), %rax
	movb	(%rax, %rcx, 1), %al
	andq	$0xFF, %rax
	movb	%al, %bl
	movq	-8(%rbp), %rax
	movb	%bl, (%rax, %r10, 1)
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.global getlocaltimestr
# function getlocaltimestr
getlocaltimestr:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	movq	CLOCK_REALTIME(%rip), %rax
	testq	%rax, %rax
	pushq	%rbx	# save temp param register %rbx to stack
	movq	%rax, %rbx
	lea	tv(%rip), %rax
# 	set input parameters
	movq	%rax, %rsi
	movq	%rbx, %rdi
	call	clock_gettime_tnsl
	popq	%rbx	# restore temp param register %rbx from stack
	movq	$0, %rax
	testq	%rax, %rax
	movq	%rax, %rcx
	lea	tv(%rip), %rax
	movq	(%rax, %rcx, 8), %rax
	testq	%rax, %rax
	movq	%rax, time_now(%rip)
	lea	time_now(%rip), %rax
# 	set input parameters
	movq	%rax, %rdi
	call	ctime
	movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

.data
	.align 8

# endlibrary
