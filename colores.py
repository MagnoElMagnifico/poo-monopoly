#!/bin/python3

for i in range(0, 16):
    for j in range(0, 16):
        code = str(i * 16 + j)
        print(f'\u001b[38;5;{code}m{code.ljust(4)}', end='')
    print('\u001b[0m')
