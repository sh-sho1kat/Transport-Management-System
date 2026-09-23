#!/usr/bin/env python3
"""Manage only this project's private PostgreSQL cluster. Linux/macOS, no Docker or sudo."""
import argparse, os, pathlib, shutil, subprocess, tempfile, shlex
root = pathlib.Path(__file__).resolve().parents[1]
state = root / '.local'
data = state / 'postgres'
port = 55432
password = 'WaylineDb123!'
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('action', choices=['start','stop','status'])
a = p.parse_args()

candidate = os.environ.get('POSTGRES_BIN')
if not candidate and shutil.which('pg_config'):
    candidate = subprocess.check_output(['pg_config','--bindir'], text=True).strip()
if not candidate or not (pathlib.Path(candidate)/'initdb').exists():
    installed = sorted(pathlib.Path('/usr/lib/postgresql').glob('*/bin/initdb'))
    candidate = str(installed[-1].parent) if installed else None
if not candidate:
    p.error('Install PostgreSQL server first (Ubuntu: sudo apt install postgresql postgresql-contrib), or set POSTGRES_BIN to its bin directory.')
bin = pathlib.Path(candidate)
def run(name, *args, **kw):
    return subprocess.run([str(bin/name),*map(str,args)],check=True,**kw)
def running():
    return data.exists() and subprocess.run([str(bin/'pg_ctl'),'-D',str(data),'status'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode == 0
if a.action == 'status':
    print('Running' if running() else 'Stopped');raise SystemExit(0)
if a.action == 'stop':
    if running(): run('pg_ctl','-D',data,'-m','fast','-w','stop')
    else: print('Already stopped.')
    raise SystemExit(0)
if hasattr(os,'geteuid') and os.geteuid() == 0:
    p.error('Run as your normal Linux user, not sudo/root.')
state.mkdir(mode=0o700,exist_ok=True)
if not (data/'PG_VERSION').exists():
    if data.exists() and any(data.iterdir()):p.error('The data directory is nonempty but not a valid cluster; inspect it manually.')
    with tempfile.NamedTemporaryFile(mode='w',prefix='wayline-pg-') as f:
        f.write(password+'\n');f.flush()
        run('initdb','-D',data,'-U','wayline','-A','scram-sha-256','--pwfile='+f.name,'--encoding=UTF8','--locale=C.UTF-8')
if not running():
    # TCP loopback only; avoid long project paths exceeding Unix socket path limits.
    options=shlex.join(['-p',str(port),'-h','127.0.0.1','-k',''])
    run('pg_ctl','-D',data,'-l',state/'postgres.log','-o',options,'-w','start')
env={**os.environ,'PGPASSWORD':password}
connection=['-h','127.0.0.1','-p',str(port),'-U','wayline']
exists=subprocess.check_output([str(bin/'psql'),*connection,'-d','postgres','-tAc',"SELECT 1 FROM pg_database WHERE datname='wayline'"],env=env,text=True).strip()
if exists!='1':run('createdb',*connection,'wayline',env=env)
run('psql',*connection,'-d','wayline','-v','ON_ERROR_STOP=1','-c','CREATE EXTENSION IF NOT EXISTS btree_gist;',env=env)
print('Ready: 127.0.0.1:55432 / wayline, user wayline. Local password: WaylineDb123!')
