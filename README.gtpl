### 🌟 About me

- A proud 🤴 of two amazing kiddos 💛
- Helping out at the family farm 🥒
- Constantly learning new tricks and skills 🤓
- Always up for family time 👪
- Bookworm and puzzle master 📘✍️
- Brainstorming life hacks to make life easier 😎
- Obsessed with AI & ML, exploring trends to create opportunities 🤖📈
- Passionate about evaluating quality in emerging tech 💻


### 👷 Check out what I'm currently working on
{{ range recentContributions 5 }}
- [{{ .Repo.Name }}]({{ .Repo.URL }}) - {{ .Repo.Description }}
{{- end }}

### 🌱 My latest projects
{{ range recentRepos 5 }}
- [{{ .Name }}]({{ .URL }}) - {{ .Description }}
{{- end }}

### 🔨 My recent Pull Requests
{{ range recentPullRequests 5 }}
- [{{ .Title }}]({{ .URL }}) on [{{ .Repo.Name }}]({{ .Repo.URL }})
{{- end }}

### 🔭 Latest releases I've contributed to
{{ range recentReleases 5 }}
- [{{ .Name }}]({{ .URL }}) [`{{ .LastRelease.TagName }}`]({{ .LastRelease.URL }}) - {{.Description}}
{{- end }}

### ⭐ Recent Stars
{{ range recentStars 5 }}
- [{{ .Repo.Name }}]({{ .Repo.URL }}) - {{ .Repo.Description }}
{{- end }}

### 💖 Recent followers
{{ range followers 5 }}
- [**@{{ .Login }}**]({{ .URL }})
{{- end }}

### 📰 Recent Blog Posts
{{ range rss "https://maximousblk.me/feed" 5 }}
- [{{ .Title }}]({{ .URL }})
{{- end }}

