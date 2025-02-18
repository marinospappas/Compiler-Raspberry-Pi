
# own implementation of _start for x86 64
.extern __bss_start
.extern _end
.extern __libc_init_first
.global _start
_start:
        # set frame
        xorq    %rbp, %rbp

        # zero bss
        pushq	%rcx
        pushq   %rdi
        pushq   %rsi
        xorq	%rcx, %rcx
        movq	__bss_start(%rip), %rdi
        movq	_end(%rip), %rsi
        subq    %rsi, %rdi      # size of bss in %rsi
zero_bss_next:
        cmpq	%rcx, %rsi	    # check for end of bss
        je	    zero_bss_end

        movb	$0, (%rdi, %rcx)	# zero memory
        inc	%rcx
        jmp	zero_bss_next
zero_bss_end:
        popq    %rsi
        popq    %rdi
        popq    %rcx

        # initialise libc
        call    __libc_init_first

        # call main
        movq    $0, %rdi        # argc currently 0
        movq    $0, %rsi        # argv currently null
        movq    $0, %rdx        # envp currently null
        xorq    %rax, %rax
        call    main

        # call exit 0
        movq    $60, %rax       # exit system call
        xorq    %rdi, %rdi      # exit code 0
        syscall                 # the end!

