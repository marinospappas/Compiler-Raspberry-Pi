###################################################################################
# time_tnsl function
# implements time related functions by calling directly the corresponding system calls
# Author: M. Pappas
# Version 1.0 18.02.2025
###################################################################################

.data
	.align 8

.text
.global sleep_nanosec
.global gettime

#############################################
# sleep_nanosec
# sleep for a number of nanosecs or secs
#     by calling the linux nanosleep system call
# uses  struct timespec {
#           time_t tv_sec;        /* seconds */
#           long   tv_nsec;       /* nanoseconds */
#        };
# params:
#   rdi:    the number of seconds to sleep
#   rsi:    the number of nanoseconds to sleep
# returns:
#   rax:    always 0
#
sleep_nanosec:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$16, %rsp

	# timespec struct in stack
	movq	%rdi, -16(%rbp)     # seconds
	movq	%rsi, -8(%rbp)      # nanoseconds

    movq    $35, %rax           # nanosleep system call
    lea     -16(%rbp), %rdi     # address of timespec
    xor     %rsi, %rsi
    syscall

    movq	%rbp, %rsp		# restore stack frame
	popq	%rbp
	popq	%rbx		# restore "callee"-save registers
	ret

######################################################################
# clock_gettime
# returns the clock time by calling the linux clock_gettime system call
# params:
#   none
# returns:
#   rax:    seconds since epoch
#   rdx:    nanoseconds in the second
#
gettime:
	pushq	%rbx		# save "callee"-save registers
	pushq	%rbp		# new stack frame
	movq	%rsp, %rbp
	subq	$16, %rsp

	# timespec struct in stack
    movq    $0, %rdi     # clock_realtime
    lea     -16(%rbp), %rsi     # address of timespec
    movq    $228, %rax   # clock_gettime system call
    syscall

    # return values
	movq	-16(%rbp), %rax     # seconds
	movq	-8(%rbp), %rdx      # nanoseconds

    movq	%rbp, %rsp   # restore stack frame
	popq	%rbp
	popq	%rbx		 # restore "callee"-save registers
	ret
