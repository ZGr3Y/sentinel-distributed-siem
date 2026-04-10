import os
import re

emoji_pattern = re.compile(
    r"["
    r"\U0001f300-\U0001f5ff"
    r"\U0001f900-\U0001f9ff"
    r"\U0001f600-\U0001f64f"
    r"\U0001f680-\U0001f6ff"
    r"\U00002600-\U000026ff"
    r"\U00002700-\U000027bf"
    r"\U0001f1e6-\U0001f1ff"
    r"\U0001f191-\U0001f251"
    r"\U0001f004"
    r"\U0001f0cf"
    r"\U0001f170-\U0001f171"
    r"\U0001f17e-\U0001f17f"
    r"\U0001f18e"
    r"\u3030"
    r"\u2b50"
    r"\u2b55"
    r"\u2934-\u2935"
    r"\u2b05-\u2b07"
    r"\u2b1b-\u2b1c"
    r"\u3297"
    r"\u3299"
    r"\u303d"
    r"\u00a9"
    r"\u00ae"
    r"\u2122"
    r"\u23f3"
    r"\u24c2"
    r"\u23e9-\u23ef"
    r"\u25b6"
    r"\u23f8-\u23fa"
    r"]+",
    re.UNICODE
)

def find_emojis(root_dir):
    for root, dirs, files in os.walk(root_dir):
        dirs[:] = [d for d in dirs if d not in ['.git', 'node_modules', 'target', 'build', '.idea', 'dist', 'coverage']]
        for file in files:
            if file.endswith('.md') or 'readme' in file.lower():
                continue
            
            filepath = os.path.join(root, file)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                    if emoji_pattern.search(content):
                        print(f"File: {filepath}")
                        for i, line in enumerate(content.split('\n')):
                            if emoji_pattern.search(line):
                                print(f"  Line {i+1}: {line.strip()}")
            except Exception:
                pass

find_emojis('.')
