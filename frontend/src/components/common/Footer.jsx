export default function Footer() {
  return (
    <footer className="mt-16 border-t border-gray-200 bg-white">
      <div className="mx-auto max-w-7xl px-4 py-8 text-sm text-gray-500">
        <div className="flex flex-col items-center justify-between gap-2 sm:flex-row">
          <p>© {new Date().getFullYear()} ShopSphere. All rights reserved.</p>
          <p>Built as a Java Spring Boot microservices + React portfolio project.</p>
        </div>
      </div>
    </footer>
  )
}
