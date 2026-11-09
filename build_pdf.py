import re
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib import colors

pdfmetrics.registerFont(TTFont('DejaVu', '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'))
pdfmetrics.registerFont(TTFont('DejaVu-Bold', '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'))

def build_pdf(md_path, pdf_path, main_title):
    doc = SimpleDocTemplate(pdf_path, pagesize=A4, rightMargin=36, leftMargin=36, topMargin=36, bottomMargin=36)
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle('Title', parent=styles['Normal'], fontName='DejaVu-Bold', fontSize=16, leading=20, textColor=colors.HexColor('#056cfd'), spaceAfter=10)
    h1_style = ParagraphStyle('H1', parent=styles['Normal'], fontName='DejaVu-Bold', fontSize=12, leading=16, textColor=colors.HexColor('#312921'), spaceBefore=10, spaceAfter=4)
    h2_style = ParagraphStyle('H2', parent=styles['Normal'], fontName='DejaVu-Bold', fontSize=10, leading=14, textColor=colors.HexColor('#1b4de3'), spaceBefore=8, spaceAfter=3)
    body_style = ParagraphStyle('Body', parent=styles['Normal'], fontName='DejaVu', fontSize=8.5, leading=12, textColor=colors.HexColor('#312921'), spaceAfter=4)
    code_style = ParagraphStyle('Code', parent=styles['Normal'], fontName='DejaVu', fontSize=7.5, leading=10, textColor=colors.HexColor('#24292e'), backColor=colors.HexColor('#f6f8fa'), leftIndent=10, rightIndent=10, spaceAfter=4)
    bullet_style = ParagraphStyle('Bullet', parent=body_style, leftIndent=12)

    elements = [Paragraph(main_title, title_style), Spacer(1, 8)]

    with open(md_path, encoding='utf-8') as f:
        lines = f.readlines()

    in_code = False
    code_buf = []

    for line in lines:
        raw = line.rstrip()
        s = raw.strip()
        if s.startswith('```'):
            in_code = not in_code
            if not in_code:
                c_text = '<br/>'.join(code_buf)
                elements.append(Paragraph(c_text, code_style))
                elements.append(Spacer(1, 4))
                code_buf = []
            continue
        if in_code:
            clean = raw.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace(' ', '&nbsp;')
            code_buf.append(clean)
            continue
        if not s:
            elements.append(Spacer(1, 3))
            continue
        if s.startswith('# '):
            continue
        if s.startswith('## '):
            elements.append(Paragraph(s[3:], h1_style))
            continue
        if s.startswith('### '):
            elements.append(Paragraph(s[4:], h2_style))
            continue
        if s.startswith('* ') or s.startswith('- '):
            text = s[2:]
            text = text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            text = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', text)
            text = re.sub(r'`(.*?)`', r'<font color="#1b4de3"><i>\1</i></font>', text)
            elements.append(Paragraph('&bull; ' + text, bullet_style))
            continue
        if s.startswith('|'):
            # simple table row formatted
            cells = [c.strip() for c in s.split('|')[1:-1]]
            if all(set(c) <= {'-', ':', ' '} for c in cells):
                continue
            row_text = ' &nbsp;|&nbsp; '.join(cells)
            row_text = row_text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            row_text = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', row_text)
            elements.append(Paragraph(f'<font size="7">{row_text}</font>', body_style))
            continue

        text = s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
        text = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', text)
        text = re.sub(r'`(.*?)`', r'<font color="#1b4de3"><i>\1</i></font>', text)
        elements.append(Paragraph(text, body_style))

    doc.build(elements)
    print('Generated:', pdf_path)

if __name__ == '__main__':
    import sys
    build_pdf(sys.argv[1], sys.argv[2], sys.argv[3])
