'use strict';

function Navbar() {

  return (
    <nav className='navbar navbar-expand-lg navbar-light bg-light'>

      <a className="navbar-brand" href="#">
        <img src="/s/images/oss_logo_32.png" width="32" height="32" className="d-inline-block align-top"
             alt="OpenSearchServer 2.0"/>
        OpenSearchServer 2.0
      </a>
      <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav"
              aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
        <span className="navbar-toggler-icon"/>
      </button>
      <div className="collapse navbar-collapse" id="navbarNav">
        <ul className="navbar-nav">
          <li className="nav-item active">
            <a className="nav-link">Accounts
              <span className="sr-only">(current)</span></a>
          </li>
          <li className="nav-item">
            <a className="nav-link" href="#">Admin</a>
          </li>
          <li className="nav-item">
            <a className="nav-link" href="#">Sign In</a>
          </li>
          <li className="nav-item"><a className="nav-link" href="#">Log out</a></li>
        </ul>
      </div>
    </nav>);
}




