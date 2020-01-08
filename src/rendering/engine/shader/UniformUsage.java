package rendering.engine.shader;

public enum UniformUsage {
	UNIFORM_USAGE_UNDEFINED{
		@Override
		public int sizeOf() {
			return 0;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_UNDEFINED";
		}
		
		@Override
		public UniformUsage dataType() {
			return null;
		}
	}
	
	, UNIFORM_USAGE_INT_16{
		@Override
		public int sizeOf() {
			return Short.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_INT_16";
		}
		
		@Override
		public UniformUsage dataType() {
			return this;
		}
	}
	
	, UNIFORM_USAGE_INT_32{
		@Override
		public int sizeOf() {
			return Integer.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_INT_32";
		}
		
		@Override
		public UniformUsage dataType() {
			return this;
		}
	}
	
	, UNIFORM_USAGE_INT_64{
		@Override
		public int sizeOf() {
			return Long.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_INT_64";
		}
		
		@Override
		public UniformUsage dataType() {
			return this;
		}
	}
	
	, UNIFORM_USAGE_FLOAT{
		@Override
		public int sizeOf() {
			return Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_FLOAT";
		}
		
		@Override
		public UniformUsage dataType() {
			return this;
		}
	}
	
	, UNIFORM_USAGE_DOUBLE{
		@Override
		public int sizeOf() {
			return Double.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_DOUBLE";
		}
		
		@Override
		public UniformUsage dataType() {
			return this;
		}
	}
	
	, UNIFORM_USAGE_VECTOR_3F{
		@Override
		public int sizeOf() {
			return 3*Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_VECTOR_3F";
		}
		
		@Override
		public UniformUsage dataType() {
			return this;
		}
	}
	
	, UNIFORM_USAGE_VECTOR_4F{
		@Override
		public int sizeOf() {
			return 4*Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_VECTOR_4F";
		}
		
		@Override
		public UniformUsage dataType() {
			return this;
		}
	}
	
	, UNIFORM_USAGE_MATRIX_4F{
		@Override
		public int sizeOf() {
			return 4*4*Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_MATRIX_4F";
		}
		
		@Override
		public UniformUsage dataType() {
			return this;
		}
	}

	, UNIFORM_USAGE_WORLD_TRANSFORM{
		@Override
		public int sizeOf() {
			return 4*4*Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_WORLD_TRANSFORM";
		}
		
		@Override
		public UniformUsage dataType() {
			return UNIFORM_USAGE_MATRIX_4F;
		}
	}
	
	, UNIFORM_USAGE_CAMERA_TRANSFORM {
		@Override
		public int sizeOf() {
			return 4*4*Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_CAMERA_TRANSFORM";
		}
		
		@Override
		public UniformUsage dataType() {
			return UNIFORM_USAGE_MATRIX_4F;
		}
	}
	, UNIFORM_USAGE_MODEL_TRANSFORM{
		@Override
		public int sizeOf() {
			return 4*4*Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_MODEL_TRANSFORM";
		}
		
		@Override
		public UniformUsage dataType() {
			return UNIFORM_USAGE_MATRIX_4F;
		}
	}

	, UNIFORM_USAGE_COLOR_3F{
		@Override
		public int sizeOf() {
			return 3*Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_COLOR_3F";
		}
		
		@Override
		public UniformUsage dataType() {
			return UNIFORM_USAGE_VECTOR_3F;
		}
	}
	
	, UNIFORM_USAGE_POSITION_3F{
		@Override
		public int sizeOf() {
			return 3*Float.BYTES;
		}

		@Override
		public String toString() {
			return "UNIFORM_USAGE_POSITION_3F";
		}
		
		@Override
		public UniformUsage dataType() {
			return UNIFORM_USAGE_VECTOR_3F;
		}
	};
	
	public abstract int sizeOf();
	public abstract String toString();
	public abstract UniformUsage dataType();
}
