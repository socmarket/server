package socmarket.twoc.environment.db

import zio.Has

package object migration {
  type Migration = Has[Migration.Service]
}