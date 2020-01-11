#coding:utf-8
__author__ = 'the5fire'
from os import path

from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler 

class Handler(BaseHTTPRequestHandler):

    def do_GET(self):
        self.send_response(200)
        self.end_headers()     
        self.wfile.write(self.render('index'))
        self.wfile.write('\n') 
        return

    def render(self, name='index'):
        file_name = '%s.html' % name
        if path.isfile(file_name):
            html = open(file_name, 'r').read()
            return html            
        return None


if __name__ == '__main__':
    server = HTTPServer(('localhost', 8181), Handler)
    print 'Development server is running at http://127.0.0.1:8181/'
    print 'Starting server, use  to stop'
    server.serve_forever()