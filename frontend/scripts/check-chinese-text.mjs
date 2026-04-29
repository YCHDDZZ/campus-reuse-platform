import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const root = path.resolve(__dirname, '..')
const srcDir = path.join(root, 'src')
const textExtensions = new Set(['.vue', '.js', '.ts', '.jsx', '.tsx', '.html'])
const suspiciousMatchers = [
  /(label|placeholder|title|description)=['"][^'"\n]*\?{2,}[^'"\n]*['"]/,
  />[^<\n]*\?{2,}[^<\n]*</,
  /['"`][^'"`\n]*\?{2,}[^'"`\n]*['"`]/,
]

const files = []
const walk = (dir) => {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      walk(fullPath)
      continue
    }
    if (textExtensions.has(path.extname(entry.name))) {
      files.push(fullPath)
    }
  }
}

walk(srcDir)
files.push(path.join(root, 'index.html'))

const issues = []
for (const file of files) {
  const content = fs.readFileSync(file, 'utf8')
  const lines = content.split(/\r?\n/)
  lines.forEach((line, index) => {
    if (line.includes('http://') || line.includes('https://')) return
    if (suspiciousMatchers.some((regex) => regex.test(line))) {
      issues.push(`${path.relative(root, file)}:${index + 1}: ${line.trim()}`)
    }
  })
}

if (issues.length) {
  console.error('检测到疑似中文乱码（连续问号），请先修复后再运行：')
  for (const item of issues) {
    console.error(`- ${item}`)
  }
  process.exit(1)
}

console.log('中文文本检查通过：未发现连续问号乱码。')
